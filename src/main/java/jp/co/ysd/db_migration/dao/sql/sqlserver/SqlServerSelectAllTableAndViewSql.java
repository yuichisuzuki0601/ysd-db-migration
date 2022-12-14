package jp.co.ysd.db_migration.dao.sql.sqlserver;

/**
 * 
 * @author yuichi
 *
 */
public class SqlServerSelectAllTableAndViewSql {

	// TODO viewを含んでいない
	private static final String TEMPLATE = "SELECT name FROM sys.objects WHERE type = 'U'";

	public static String get() {
		return TEMPLATE;
	}

}
