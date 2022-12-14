package jp.co.ysd.db_migration.dao.sql;

import jp.co.ysd.db_migration.util.Template;

/**
 * 
 * @author yuichi
 *
 */
public final class CreateViewSql {

	private static final Template TEMPLATE = Template.of("CREATE OR REPLACE VIEW `{viewName}` AS {targetSql};");

	public static String get(String viewName, String targetSql) {
		return TEMPLATE.bind(viewName, targetSql);
	}

}
