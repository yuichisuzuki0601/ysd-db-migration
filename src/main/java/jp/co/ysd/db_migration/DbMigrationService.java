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
import jp.co.ysd.db_migration.sql_compiler.SqlCompiler;
import jp.co.ysd.db_migration.util.CsvToJsonTranspiler;
import jp.co.ysd.db_migration.util.FileAccessor;
import jp.co.ysd.ysd_util.string.YsdStringUtil;

/**
 * @author yuichi
 */
@Service
public class DbMigrationService {

	private Logger l = LoggerFactory.getLogger(getClass());

	@Autowired
	private FileChecker fileChecker;

	@Autowired
	private DaoManager factory;

	@Autowired
	private SqlCompiler sqlCompiler;

	@Transactional
	public void execute(ExecMode mode, String rootDir, String dataDir) throws Exception {
		l.info("db-migration service start.");

		FileAccessor.init(rootDir, dataDir);

		l.info("mode: {}", mode);
		l.info("root directory: {}", FileAccessor.getRootDir());

		var dao = factory.get();
		if (mode.some(ExecMode.REPLACEINDEX, ExecMode.DROPINDEX)) {
			prepareIndex(mode, dao);
			return;
		}
		if (mode.is(ExecMode.REPLACEVIEW)) {
			prepareView(mode, dao);
			return;
		}
		// ***
		if (mode.some(ExecMode.NORMAL, ExecMode.REBUILD)) {
			fileChecker.checkAllFiles();
		}
		if (mode.some(ExecMode.REBUILD, ExecMode.DROPALL)) {
			dao.dropAllForeignKey();
			dao.dropAllTableAndView();
		}
		if (mode.not(ExecMode.DROPALL)) {
			var createds = prepareTable(mode, dao);
			prepareIndex(mode, dao, createds);
			prepareData(mode, dao, createds);
			prepareConstraint(mode, dao, createds);
			prepareView(mode, dao);
			applySql(dao);
		}

		l.info("db-migration service finish.");
	}

	@SuppressWarnings("unchecked")
	private List<String> prepareTable(ExecMode mode, Dao dao) throws IOException {
		var createds = new ArrayList<String>();
		if (mode.some(ExecMode.NORMAL, ExecMode.REBUILD)) {
			var defineFiles = FileAccessor.getDefineFiles();
			if (defineFiles != null) {
				for (var defineFile : defineFiles) {
					var tableName = FilenameUtils.removeExtension(defineFile.getName());
					var define = new ObjectMapper().readValue(defineFile, Map.class);
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
		var index = new ObjectMapper().readValue(indexFile, Map.class);
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
		if (mode.some(ExecMode.NORMAL, ExecMode.REBUILD, ExecMode.REPLACEINDEX, ExecMode.DROPINDEX)) {
			var indexFiles = FileAccessor.getIndexFiles();
			for (var indexFile : indexFiles) {
				var tableName = FilenameUtils.removeExtension(indexFile.getName()).replaceAll("-index", "");
				prepareIndexCore(mode, dao, indexFile, tableName);
			}
		}
	}

	private void prepareIndex(ExecMode mode, Dao dao, List<String> createds) throws IOException {
		if (mode.some(ExecMode.NORMAL, ExecMode.REBUILD, ExecMode.REPLACEINDEX, ExecMode.DROPINDEX)) {
			for (var tableName : createds) {
				var indexFile = FileAccessor.getIndexFile(tableName);
				if (indexFile.exists()) {
					prepareIndexCore(mode, dao, indexFile, tableName);
				}
			}
		}
	}

	private void prepareData(ExecMode mode, Dao dao, List<String> createds) throws IOException {
		File[] dataFiles = FileAccessor.getOrderdDataFiles();
		if (dataFiles == null) {
			return;
		}
		for (File dataFile : dataFiles) {
			if (dataFile.exists()) {
				String tableName = FilenameUtils.removeExtension(dataFile.getName()).replace("-data", "");
				boolean cond1 = (mode == ExecMode.NORMAL || mode == ExecMode.REBUILD) && createds.contains(tableName);
				boolean cond2 = mode == ExecMode.DATAALL;
				if (cond1 || cond2) {
					String extension = FilenameUtils.getExtension(dataFile.getName());
					if ("csv".equals(extension)) {
						prepareCsvData(dataFile, tableName, dao);
					} else if ("json".equals(extension)) {
						prepareJsonData(dataFile, tableName, dao);
					}
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void prepareJsonData(File dataFile, String tableName, Dao dao) throws IOException {
		String json = new String(Files.readAllBytes(Paths.get(dataFile.getPath())));
		dao.insert(tableName, new ObjectMapper().readValue(json, List.class));
	}

	@SuppressWarnings("unchecked")
	private void prepareCsvData(File dataFile, String tableName, Dao dao) throws IOException {
		String json = CsvToJsonTranspiler.transpile(dataFile.getPath());
		dao.bulkInsert(tableName, new ObjectMapper().readValue(json, List.class));
	}

	@SuppressWarnings("unchecked")
	private void prepareConstraint(ExecMode mode, Dao dao, List<String> createds) throws IOException {
		if (!(mode == ExecMode.NORMAL || mode == ExecMode.REBUILD)) {
			return;
		}
		for (String tableName : createds) {
			File constraintFile = FileAccessor.getConstraintFile(tableName);
			if (constraintFile.exists()) {
				Map<String, Object> constraint = new ObjectMapper().readValue(constraintFile, Map.class);
				List<Map<String, Object>> cols = (List<Map<String, Object>>) constraint.get("cols");
				dao.createForeignKey(tableName, cols);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void prepareView(ExecMode mode, Dao dao) throws IOException {
		if (!(mode == ExecMode.NORMAL || mode == ExecMode.REBUILD || mode == ExecMode.REPLACEVIEW)) {
			return;
		}
		File[] viewFiles = FileAccessor.getViewFiles();
		for (File viewFile : viewFiles) {
			String viewName = FilenameUtils.removeExtension(viewFile.getName()).replaceAll("-view", "");
			Map<String, Object> viewSetting = new ObjectMapper().readValue(viewFile, Map.class);
			try {
				dao.createView(mode == ExecMode.REPLACEVIEW, viewName, viewSetting);
			} catch (Exception e) {
				if (mode != ExecMode.REPLACEVIEW) {
					throw new IOException(e);
				}
			}
		}
	}

	private void applySql(Dao dao) throws Exception {
		File[] sqlFiles = FileAccessor.getOrderdSqlFiles();
		if (sqlFiles == null) {
			return;
		}
		for (File sqlFile : sqlFiles) {
			if (sqlFile.exists()) {
				String extension = FilenameUtils.getExtension(sqlFile.getName());
				if ("sql".equals(extension)) {
					String sqls = YsdStringUtil.strip(Files.lines(sqlFile.toPath())
							.filter(l -> StringUtils.hasText(l) && !l.startsWith("//") && !l.startsWith("--"))
							.reduce("", (l, r) -> l + r + " "));
					StringBuilder compiled = new StringBuilder();
					for (String sql : sqls.split(";")) {
						compiled.append(sqlCompiler.compile(sql.trim()));
					}
					l.info(sqlFile.getName() + " will be executed.");
					dao.execute(compiled.toString());
				}
			}
		}
	}

}
