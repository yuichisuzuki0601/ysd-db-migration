package jp.co.ysd.db_migration.dao.sql;

import jp.co.ysd.ysd_util.string.Template;

/**
 * 
 * @author yuichi
 *
 */
public final class DropViewSql {

	private static final Template TEMPLATE = Template.of("DROP VIEW `{viewName}`;");

	public static String get(String tableName) {
		return TEMPLATE.bind(tableName);
	}

}
