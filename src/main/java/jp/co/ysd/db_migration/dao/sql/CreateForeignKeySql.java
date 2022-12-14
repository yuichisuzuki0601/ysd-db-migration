package jp.co.ysd.db_migration.dao.sql;

import jp.co.ysd.ysd_util.string.Template;

/**
 * 
 * @author yuichi
 *
 */
public final class CreateForeignKeySql {

	private static final Template TEMPLATE = Template
			.of("ALTER TABLE `{tableName}` ADD FOREIGN KEY (`{column}`) REFERENCES `{targetTableName}` (`{targetColumn}`) {option};");

	public static String get(String tableName, String column, String targetTableName, String targetColumn,
			String option) {
		return TEMPLATE.bind(tableName, column, targetTableName, targetColumn, option);
	}

}
