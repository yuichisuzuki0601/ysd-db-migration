package jp.co.ysd.db_migration.dao.sql.mysql;

import jp.co.ysd.db_migration.util.Template;

/**
 * 
 * @author yuichi
 *
 */
public class MySqlExistTableAndViewSql {

	private static final Template TEMPLATE = Template.of("SHOW TABLES FROM {schema} LIKE ?;");

	public static String get(String schema) {
		return TEMPLATE.bind(schema);
	}

}
