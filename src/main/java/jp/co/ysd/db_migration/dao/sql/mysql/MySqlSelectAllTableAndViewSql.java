package jp.co.ysd.db_migration.dao.sql.mysql;

import jp.co.ysd.db_migration.util.Template;

/**
 * 
 * @author yuichi
 *
 */
public class MySqlSelectAllTableAndViewSql {

	private static final Template TEMPLATE = Template.of("""
				SELECT
					TABLE_NAME AS name,
					TABLE_TYPE AS type
				FROM
					INFORMATION_SCHEMA.TABLES
				WHERE
					TABLE_SCHEMA = '{schema}';
			""");

	public static String get(String schema) {
		return TEMPLATE.bind(schema);
	}

}
