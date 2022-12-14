package jp.co.ysd.db_migration.dao.sql;

import jp.co.ysd.ysd_util.string.Template;

/**
 * 
 * @author yuichi
 *
 */
public final class DropIndexSql {

	private static final Template TEMPLATE = Template.of("DROP INDEX `{indexName}` ON `{tableName}`;");

	public static String get(String indexName, String tableName) {
		return TEMPLATE.bind(indexName, tableName);
	}

}
