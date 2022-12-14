package jp.co.ysd.db_migration.dao.sql.sqlserver;

/**
 * 
 * @author yuichi
 *
 */
public class SqlServerExistTableAndViewSql {

	// TODO viewを含んでいない
	private static final String TEMPLATE = "SELECT 1 FROM Sys.Tables WHERE name = ?";

	public static String get() {
		return TEMPLATE;
	}

}
