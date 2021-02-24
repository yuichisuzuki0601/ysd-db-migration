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
import jp.co.ysd.db_migration.util.SpaceFormatter;

/**
 * @author yuichi
 */
@Service
public class DbMigrationService {

	private Logger l = LoggerFactory.getLogger(getClass());

	@Autowired
	private DaoFactory factory;

	@Autowired
	private SqlCompiler sqlCompiler;

	@Transactional
	public void execute(ExecMode mode, String rootDir, String dataDir) throws Exception {
		l.info("db-migration service start.");
		FileAccessor.init(rootDir, dataDir);
		l.info("mode: " + mode);
		l.info("root directory: " + FileAccessor.getRootDir());
		if (mode == ExecMode.NORMAL || mode == ExecMode.REBUILD) {
			checkAllFiles();
		}
		Dao dao = factory.get();
		if (mode == ExecMode.REBUILD || mode == ExecMode.DROPALL) {
			dao.dropAllForeignKey();
			dao.dropAllTable();
		}
		if (mode != ExecMode.DROPALL) {
			List<String> createds = prepareTable(mode, dao);
			prepareData(mode, dao, createds);
			prepareConstraint(mode, dao, createds);
			applySql(dao);
		}
		l.info("db-migration service finish.");
	}

	private void checkAllFiles() throws Exception {
		List<String> tableNames = new ArrayList<>();
		for (File defineFile : FileAccessor.getDefineFiles()) {
			tableNames.add(FilenameUtils.removeExtension(defineFile.getName()));
		}
		List<String> incorrectFiles = new ArrayList<>();
		for (File file : FileAccessor.getIndexFiles()) {
			String fileName = file.getName();
			if (!tableNames.contains(FilenameUtils.removeExtension(fileName.replaceAll("-index", "")))) {
				incorrectFiles.add(fileName);
			}
		}
		for (File file : FileAccessor.getConstraintFiles()) {
			String fileName = file.getName();
			if (!tableNames.contains(FilenameUtils.removeExtension(fileName.replaceAll("-constraint", "")))) {
				incorrectFiles.add(fileName);
			}
		}
		for (File file : FileAccessor.getDataFiles()) {
			String fileName = file.getName();
			if (!tableNames.contains(FilenameUtils.removeExtension(fileName.replaceAll("-data", "")))) {
				incorrectFiles.add(fileName);
			}
		}
		if (!incorrectFiles.isEmpty()) {
			throw new RuntimeException("Incorrect files are mixed. " + incorrectFiles.toString());
		}
	}

	@SuppressWarnings("unchecked")
	private List<String> prepareTable(ExecMode mode, Dao dao) throws IOException {
		List<String> createds = new ArrayList<>();
		if (!(mode == ExecMode.NORMAL || mode == ExecMode.REBUILD)) {
			return createds;
		}
		File[] defineFiles = FileAccessor.getDefineFiles();
		if (defineFiles == null) {
			return createds;
		}
		for (File defineFile : defineFiles) {
			String tableName = FilenameUtils.removeExtension(defineFile.getName());
			Map<String, Object> define = new ObjectMapper().readValue(defineFile, Map.class);
			List<Map<String, String>> cols = (List<Map<String, String>>) define.get("cols");
			String pk = (String) define.get("pk");
			Object uq = define.get("uq");
			if (dao.createTable(mode, tableName, cols, pk, uq)) {
				File indexFile = FileAccessor.getIndexFile(tableName);
				if (indexFile.exists()) {
					Map<String, Object> index = new ObjectMapper().readValue(indexFile, Map.class);
					List<Map<String, Object>> indexCols = (List<Map<String, Object>>) index.get("cols");
					dao.createIndex(tableName, indexCols);
				}
				createds.add(tableName);
			}
		}
		return createds;
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

	private void applySql(Dao dao) throws Exception {
		File[] sqlFiles = FileAccessor.getOrderdSqlFiles();
		if (sqlFiles == null) {
			return;
		}
		for (File sqlFile : sqlFiles) {
			if (sqlFile.exists()) {
				String extension = FilenameUtils.getExtension(sqlFile.getName());
				if ("sql".equals(extension)) {
					String sqls = SpaceFormatter.format(Files.lines(sqlFile.toPath())
							.filter(l -> !StringUtils.isEmpty(l) && !l.startsWith("//") && !l.startsWith("--"))
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
