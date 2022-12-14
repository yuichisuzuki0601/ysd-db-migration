package jp.co.ysd.db_migration.dao.sql.sqlserver;

import jp.co.ysd.db_migration.util.Template;

/**
 * 
 * @author yuichi
 *
 */
public class SqlServerSelectAllIndexFromTableSql {

	// TODO 実装
	private static final Template TEMPLATE = Template.of("");

	public static String get(String tableName) {
		return TEMPLATE.bind(tableName);
	}

}
