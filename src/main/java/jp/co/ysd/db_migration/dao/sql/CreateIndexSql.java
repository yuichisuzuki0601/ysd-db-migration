package jp.co.ysd.db_migration.dao.sql;

import jp.co.ysd.db_migration.util.Template;

/**
 * 
 * @author yuichi
 *
 */
public final class CreateIndexSql {

	private static final Template TEMPLATE = Template.of("CREATE INDEX `{indexName}` ON `{tableName}` ({column});");

	public static String get(String indexName, String tableName, String column) {
		return TEMPLATE.bind(indexName, tableName, column);
	}

}
