package jp.co.ysd.db_migration.dao.sql.sqlserver;

import jp.co.ysd.ysd_util.string.Template;

/**
 * 
 * @author yuichi
 *
 */
public class SqlServerDropForeignKeySql {

	private static final Template TEMPLATE = Template.of("ALTER TABLE {tableName} DROP CONSTRAINT {foreignKey};");

	public static String get(String tableName, String foreignKey) {
		return TEMPLATE.bind(tableName, foreignKey);
	}

}
