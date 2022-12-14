package jp.co.ysd.db_migration.dao.sql.mysql;

import jp.co.ysd.db_migration.util.Template;

/**
 * 
 * @author yuichi
 *
 */
public class MySqlDropForeignKeySql {

	private static final Template TEMPLATE = Template.of("ALTER TABLE {tableName} DROP FOREIGN KEY {foreignKey};");

	public static String get(String tableName, String foreignKey) {
		return TEMPLATE.bind(tableName, foreignKey);
	}

}
