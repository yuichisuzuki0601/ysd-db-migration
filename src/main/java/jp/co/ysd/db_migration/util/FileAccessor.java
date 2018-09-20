package jp.co.ysd.db_migration.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;

import org.springframework.util.StringUtils;

/**
 *
 * @author yuichi
 *
 */
public final class FileAccessor {

	private static final String DEFAULT_ROOT_DIR = "./database";
	private static final String DEFAULT_DATA_DIR = "%s/data";
	private static final String DEFINE_DIR = "%s/define";
	private static final String DEFINE_FILE = "%s/define/%s.json";
	private static final String INDEX_FILE = "%s/index/%s-index.json";
	private static final String CONSTRAINT_DIR = "%s/constraint";
	private static final String CONSTRAINT_FILE = "%s/constraint/%s-constraint.json";
	private static final String SQL_DIR = "%s/sql";

	private static String rootDir;
	private static String dataDir;

	private FileAccessor() {
	}

	public static void init(String _rootDir, String _dataDir) {
		rootDir = _rootDir != null ? _rootDir : DEFAULT_ROOT_DIR;
		dataDir = _dataDir != null ? "%s/" + _dataDir : DEFAULT_DATA_DIR;
	}

	public static String getRootDir() {
		return rootDir;
	}

	public static File[] getDefineFiles() {
		return new File(String.format(DEFINE_DIR, rootDir)).listFiles();
	}

	public static File getDefineFile(String tableName) {
		return new File(String.format(DEFINE_FILE, rootDir, tableName));
	}

	public static File getIndexFile(String tableName) {
		return new File(String.format(INDEX_FILE, rootDir, tableName));
	}

	public static File[] getOrderdDataFiles() throws IOException {
		return getOrderdFiles(dataDir);
	}

	public static File[] getConstraintFiles() {
		return new File(String.format(CONSTRAINT_DIR, rootDir)).listFiles();
	}

	public static File getConstraintFile(String tableName) {
		return new File(String.format(CONSTRAINT_FILE, rootDir, tableName));
	}

	public static File[] getOrderdSqlFiles() throws IOException {
		return getOrderdFiles(SQL_DIR);
	}

	private static File[] getOrderdFiles(String dir) throws IOException {
		File[] result = null;
		File orderFile = new File(String.format(dir + "/order.txt", rootDir));
		if (orderFile.exists()) {
			result = Files.lines(orderFile.toPath()).filter(l -> !StringUtils.isEmpty(l) && !l.startsWith("//"))
					.map(l -> new File(String.format(dir, rootDir) + "/" + l)).toArray(File[]::new);
		} else {
			result = new File(String.format(dir, rootDir)).listFiles();
			if (result != null) {
				// orderファイルがない場合は名前順
				Arrays.sort(result, (f1, f2) -> f1.getName().compareTo(f2.getName()));
			}
		}
		return result;
	}
}
