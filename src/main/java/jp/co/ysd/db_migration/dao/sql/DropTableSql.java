package jp.co.ysd.db_migration.dao.sql;

import jp.co.ysd.ysd_util.string.Template;

/**
 * 
 * @author yuichi
 *
 */
public final class DropTableSql {

	private static final Template TEMPLATE = Template.of("DROP TABLE `{tableName}`;");

	public static String get(String tableName) {
		return TEMPLATE.bind(tableName);
	}

}
