package jp.co.ysd.db_migration.dao.sql;

import jp.co.ysd.db_migration.util.Template;

/**
 * 
 * @author yuichi
 *
 */
public final class SelectDataByIdSql {

	private static final Template TEMPLATE = Template.of("SELECT `{column}` FROM `{tableName}` WHERE id = {id};");

	public static String get(String column, String tableName, String id) {
		return TEMPLATE.bind(column, tableName, id);
	}

}
