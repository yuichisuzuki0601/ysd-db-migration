package jp.co.ysd.db_migration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import jp.co.ysd.db_migration.dao.Dao;
import jp.co.ysd.db_migration.dao.DaoManager;
import jp.co.ysd.db_migration.sql_compiler.SqlCompiler;
import jp.co.ysd.db_migration.util.CsvToJsonTranspiler;
import jp.co.ysd.db_migration.util.FileAccessor;
import jp.co.ysd.db_migration.util.FileChecker;
import jp.co.ysd.ysd_util.string.YsdStringUtil;

/**
 * @author yuichi
 */
@Service
public class DbMigrationService {

	private static final ObjectMapper OM = new ObjectMapper();

	@Autowired
	private DaoManager daoManager;

	@Autowired
	private FileChecker fileChecker;

	@Autowired
	private SqlCompiler sqlCompiler;

	private Logger l = LoggerFactory.getLogger(getClass());

	public void execute() throws Exception {
		l.info("db-migration service start.");

		var cm = CommandManager.getInstance();
		var mode = cm.getMode();

		FileAccessor.init(cm.getRootDir(), cm.getDataDir());

		l.info("mode: {}", mode);
		l.info("root directory: {}", FileAccessor.getRootDir());

		var dao = daoManager.get();
		if (mode.is(ExecMode.DROPSCHEMA)) {
			dropSchemaWithoutExcludes(mode, dao, cm.getExcludeSchemas());
			return;
		}
		if (mode.some(ExecMode.REPLACEINDEX, ExecMode.DROPINDEX)) {
			prepareIndex(mode, dao);
			return;
		}
		if (mode.is(ExecMode.REPLACEVIEW)) {
			prepareView(mode, dao);
			return;
		}

		// ***

		if (mode.some(ExecMode.APPLY, ExecMode.REBUILD)) {
			fileChecker.checkAllFiles();
		}

		var autoSchema = CommandManager.getInstance().getAutoSchema();
		try {
			if (autoSchema) {
				dao.createSchemaIfNotExists();
			}
			if (mode.some(ExecMode.REBUILD, ExecMode.DROPALL, ExecMode.DEFINEALL)) {
				dao.dropAllForeignKey();
				dao.dropAllTableAndView();
			}
			if (mode.not(ExecMode.DROPALL)) {
				var createds = prepareTable(mode, dao);
				prepareIndex(mode, dao, createds);
				if (mode.not(ExecMode.DEFINEALL)) {
					prepareData(mode, dao, createds);
				}
				prepareConstraint(mode, dao, createds);
				prepareView(mode, dao);
				applySql(dao);
			} else {
				if (autoSchema) {
					dao.dropSchemaIfExists();
				}
			}
		} catch (Exception e) {
			if (autoSchema) {
				dao.dropSchemaIfExists();
			}
			throw e;
		}

		l.info("db-migration service finish.");
	}

	@SuppressWarnings("unchecked")
	private List<String> prepareTable(ExecMode mode, Dao dao) throws IOException {
		var createds = new ArrayList<String>();
		if (mode.some(ExecMode.APPLY, ExecMode.REBUILD, ExecMode.DEFINEALL)) {
			var defineFiles = FileAccessor.getDefineFiles();
			if (defineFiles != null) {
				for (var defineFile : defineFiles) {
					var tableName = FilenameUtils.removeExtension(defineFile.getName());
					var define = OM.readValue(defineFile, Map.class);
					var cols = (List<Map<String, String>>) define.get("cols");
					var pk = (String) define.get("pk");
					var uq = define.get("uq");
					if (dao.createTable(mode, tableName, cols, pk, uq)) {
						createds.add(tableName);
					}
				}
			}
		}
		return createds;
	}

	@SuppressWarnings("unchecked")
	private void prepareIndexCore(ExecMode mode, Dao dao, File indexFile, String tableName) throws IOException {
		var index = OM.readValue(indexFile, Map.class);
		var indexCols = (List<Map<String, String>>) index.get("cols");
		try {
			dao.dropIndexFromTable(tableName);
			if (mode.not(ExecMode.DROPINDEX)) {
				dao.createIndex(tableName, indexCols);
			}
		} catch (Exception e) {
			if (mode.some(ExecMode.REPLACEINDEX, ExecMode.DROPINDEX)) {
				System.err.println(e.getMessage());
			} else {
				throw new IOException(e);
			}
		}
	}

	private void prepareIndex(ExecMode mode, Dao dao) throws IOException {
		if (mode.some(ExecMode.APPLY, ExecMode.REBUILD, ExecMode.REPLACEINDEX, ExecMode.DROPINDEX)) {
			var indexFiles = FileAccessor.getIndexFiles();
			for (var indexFile : indexFiles) {
				var tableName = FilenameUtils.removeExtension(indexFile.getName()).replaceAll("-index", "");
				prepareIndexCore(mode, dao, indexFile, tableName);
			}
		}
	}

	private void prepareIndex(ExecMode mode, Dao dao, List<String> createds) throws IOException {
		if (mode.some(ExecMode.APPLY, ExecMode.REBUILD, ExecMode.REPLACEINDEX, ExecMode.DROPINDEX)) {
			for (var tableName : createds) {
				var indexFile = FileAccessor.getIndexFile(tableName);
				if (indexFile.exists()) {
					prepareIndexCore(mode, dao, indexFile, tableName);
				}
			}
		}
	}

	@Transactional
	@SuppressWarnings("unchecked")
	private void prepareData(ExecMode mode, Dao dao, List<String> createds) throws IOException {
		var dataFiles = FileAccessor.getOrderdDataFiles();
		if (dataFiles != null) {
			for (var dataFile : dataFiles) {
				if (dataFile.exists()) {
					var tableName = FilenameUtils.removeExtension(dataFile.getName()).replace("-data", "");
					var cond1 = mode.some(ExecMode.APPLY, ExecMode.REBUILD) && createds.contains(tableName);
					var cond2 = mode.is(ExecMode.DATAALL);
					if (cond1 || cond2) {
						var extension = FilenameUtils.getExtension(dataFile.getName());
						if ("csv".equals(extension)) {
							var json = CsvToJsonTranspiler.transpile(dataFile.getPath());
							dao.bulkInsert(tableName, OM.readValue(json, List.class));
						} else if ("json".equals(extension)) {
							var json = new String(Files.readAllBytes(Paths.get(dataFile.getPath())));
							dao.insert(tableName, OM.readValue(json, List.class));
						}
					}
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void prepareConstraint(ExecMode mode, Dao dao, List<String> createds) throws IOException {
		if (mode.some(ExecMode.APPLY, ExecMode.REBUILD, ExecMode.DEFINEALL)) {
			for (var tableName : createds) {
				var constraintFile = FileAccessor.getConstraintFile(tableName);
				if (constraintFile.exists()) {
					Map<String, Object> constraint = OM.readValue(constraintFile, Map.class);
					var cols = (List<Map<String, Object>>) constraint.get("cols");
					dao.createForeignKey(tableName, cols);
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void prepareView(ExecMode mode, Dao dao) throws IOException {
		if (mode.some(ExecMode.APPLY, ExecMode.REBUILD, ExecMode.DEFINEALL, ExecMode.REPLACEVIEW)) {
			var viewFiles = FileAccessor.getViewFiles();
			for (var viewFile : viewFiles) {
				var viewName = FilenameUtils.removeExtension(viewFile.getName()).replaceAll("-view", "");
				Map<String, Object> viewSetting = OM.readValue(viewFile, Map.class);
				try {
					dao.createView(mode.is(ExecMode.REPLACEVIEW), viewName, viewSetting);
				} catch (Exception e) {
					if (mode.not(ExecMode.REPLACEVIEW)) {
						throw new IOException(e);
					}
				}
			}
		}
	}

	@Transactional
	private void applySql(Dao dao) throws Exception {
		var sqlFiles = FileAccessor.getOrderdSqlFiles();
		if (sqlFiles != null) {
			for (var sqlFile : sqlFiles) {
				if (sqlFile.exists()) {
					var extension = FilenameUtils.getExtension(sqlFile.getName());
					if ("sql".equals(extension)) {
						var sqls = YsdStringUtil.strip(Files.lines(sqlFile.toPath())
								.filter(l -> StringUtils.hasText(l) && !l.startsWith("//") && !l.startsWith("--"))
								.reduce("", (l, r) -> l + r + " "));
						var compiled = new StringBuilder();
						for (var sql : sqls.split(";")) {
							compiled.append(sqlCompiler.compile(sql.trim()));
						}
						l.info(sqlFile.getName() + " will be executed.");
						dao.execute(compiled.toString());
					}
				}
			}
		}
	}

	private void dropSchemaWithoutExcludes(ExecMode mode, Dao dao, String excludeSchemas) {
		var excludeSchemaList = List.of(excludeSchemas.split(","));
		var candidates = dao.selectAllSchemas().stream().filter(s -> !excludeSchemaList.contains(s)).toList();
		for (var candidate : candidates) {
			dao.execute("DROP DATABASE `" + candidate + "`");
		}
	}

}
