package jp.co.ysd.db_migration.dao.sql.mysql;

import jp.co.ysd.ysd_util.string.Template;

/**
 * 
 * @author yuichi
 *
 */
public class MySqlSelectAllIndexFromTableSql {

	private static final Template TEMPLATE = Template.of("SHOW INDEX FROM {tableName} WHERE Key_name LIKE 'idx_%%';");

	public static String get(String tableName) {
		return TEMPLATE.bind(tableName);
	}

}
