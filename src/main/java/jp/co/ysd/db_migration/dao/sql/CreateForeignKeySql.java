package jp.co.ysd.db_migration.dao.sql;

import org.springframework.util.StringUtils;

import jp.co.ysd.ysd_util.string.Template;

/**
 * 
 * @author yuichi
 *
 */
public final class CreateForeignKeySql {

	private static final Template TEMPLATE = Template
			.of("ALTER TABLE `{tableName}` ADD FOREIGN KEY (`{column}`) REFERENCES {schemaPart}`{targetTableName}` (`{targetColumn}`) {option};");

	public static String get(String tableName, String column, String schema, String targetTableName,
			String targetColumn, String option) {
		var schemaPart = StringUtils.hasText(schema) ? "`" + schema + "`." : "";
		return TEMPLATE.bind(tableName, column, schemaPart, targetTableName, targetColumn, option);
	}

}
