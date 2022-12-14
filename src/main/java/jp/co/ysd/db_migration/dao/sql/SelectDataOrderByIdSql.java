package jp.co.ysd.db_migration.dao.sql;

import jp.co.ysd.db_migration.util.Template;

/**
 * 
 * @author yuichi
 *
 */
public final class SelectDataOrderByIdSql {

	private static final Template TEMPLATE = Template.of("SELECT `{column}` FROM `{tableName}` ORDER BY id;");

	public static String get(String column, String tableName) {
		return TEMPLATE.bind(column, tableName);
	}

}
