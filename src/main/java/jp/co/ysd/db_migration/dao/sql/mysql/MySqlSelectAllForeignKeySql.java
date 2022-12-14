package jp.co.ysd.db_migration.dao.sql.mysql;

import jp.co.ysd.ysd_util.string.Template;

/**
 * 
 * @author yuichi
 *
 */
public class MySqlSelectAllForeignKeySql {

	private static final Template TEMPLATE = Template.of("""
			SELECT
				f1.constraint_name AS foreign_key_name,
				f2.table_name AS table_name
			FROM
				information_schema.table_constraints f1
			INNER JOIN
				information_schema.key_column_usage f2
			ON
				f1.table_schema = f2.table_schema
			AND
				f1.constraint_name = f2.constraint_name
			WHERE
				f1.constraint_type = 'FOREIGN KEY'
			AND
				f2.constraint_schema = '{schema}';
			""");

	public static String get(String schema) {
		return TEMPLATE.bind(schema);
	}

}
