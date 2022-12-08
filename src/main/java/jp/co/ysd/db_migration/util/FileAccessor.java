package jp.co.ysd.db_migration.util;

import static jp.co.ysd.ysd_util.stream.StreamWrapperFactory.*;

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
	private static final String DEFINE_FILE = DEFINE_DIR + "/%s.json";
	private static final String INDEX_DIR = "%s/index";
	private static final String INDEX_FILE = INDEX_DIR + "/%s-index.json";
	private static final String CONSTRAINT_DIR = "%s/constraint";
	private static final String CONSTRAINT_FILE = CONSTRAINT_DIR + "/%s-constraint.json";
	private static final String VIEW_DIR = "%s/view";
	private static final String VIEW_TEMPLATE_FILE = VIEW_DIR + "/%s";
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

	public static String getDataDir() {
		return String.format(dataDir, rootDir);
	}

	public static File[] getDefineFiles() {
		File[] result = new File(String.format(DEFINE_DIR, rootDir)).listFiles();
		return result != null ? removeIgnoreFiles(result) : new File[0];
	}

	public static File getDefineFile(String tableName) {
		return new File(String.format(DEFINE_FILE, rootDir, tableName));
	}

	public static File[] getIndexFiles() {
		File[] result = new File(String.format(INDEX_DIR, rootDir)).listFiles();
		return result != null ? removeIgnoreFiles(result) : new File[0];
	}

	public static File getIndexFile(String tableName) {
		return new File(String.format(INDEX_FILE, rootDir, tableName));
	}

	public static File[] getConstraintFiles() {
		File[] result = new File(String.format(CONSTRAINT_DIR, rootDir)).listFiles();
		return result != null ? removeIgnoreFiles(result) : new File[0];
	}

	public static File getConstraintFile(String tableName) {
		return new File(String.format(CONSTRAINT_FILE, rootDir, tableName));
	}

	public static File[] getViewFiles() {
		File[] result = new File(String.format(VIEW_DIR, rootDir)).listFiles();
		result = result != null ? removeIgnoreFiles(result) : new File[0];
		return stream(result).filter(f -> f.getName().endsWith("-view.json")).end().toArray(new File[0]);
	}

	public static File getViewTemplateFile(String filePath) {
		return new File(String.format(VIEW_TEMPLATE_FILE, rootDir, filePath));
	}

	public static File[] getDataFiles() {
		File[] result = new File(getDataDir()).listFiles();
		return result != null ? removeIgnoreFiles(result) : new File[0];
	}

	public static File[] getOrderdDataFiles() throws IOException {
		return getOrderdFiles(dataDir);
	}

	public static File[] getOrderdSqlFiles() throws IOException {
		return getOrderdFiles(SQL_DIR);
	}

	private static File[] removeIgnoreFiles(File[] files) {
		return Arrays.stream(files).filter(f -> {
			String fileName = f.getName();
			boolean cond1 = !".gitkeep".equals(fileName);
			boolean cond2 = !"order.txt".equals(fileName);
			return cond1 && cond2;
		}).toArray(File[]::new);
	}

	private static File[] getOrderdFiles(String _dir) throws IOException {
		String dir = String.format(_dir, rootDir);
		File[] result = null;
		File orderFile = new File(dir + "/order.txt");
		if (orderFile.exists()) {
			result = Files.lines(orderFile.toPath()).filter(l -> {
				return StringUtils.hasText(l) && !l.startsWith("//");
			}).map(l -> new File(dir + "/" + l)).toArray(File[]::new);
		} else {
			result = new File(dir).listFiles();
			if (result != null) {
				// orderファイルがない場合は名前順
				Arrays.sort(result, (f1, f2) -> f1.getName().compareTo(f2.getName()));
			}
		}
		return result;
	}
}
