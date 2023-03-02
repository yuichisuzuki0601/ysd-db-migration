package jp.co.ysd.db_migration.dao.sql;

import org.springframework.util.StringUtils;

import jp.co.ysd.ysd_util.string.Template;

/**
 * 
 * @author yuichi
 *
 */
public final class SelectDataOrderByIdSql {

	private static final Template TEMPLATE = Template
			.of("SELECT `{column}` FROM {schemaPart}`{tableName}` ORDER BY id;");

	public static String get(String column, String schema, String tableName) {
		var schemaPart = StringUtils.hasText(schema) ? "`" + schema + "`." : "";
		return TEMPLATE.bind(column, schemaPart, tableName);
	}

}
