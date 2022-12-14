package jp.co.ysd.db_migration.dao.sql.sqlserver;

/**
 * 
 * @author yuichi
 *
 */
public class SqlServerSelectAllForeignKeySql {

	private static final String TEMPLATE = """
					SELECT
						f.name AS foreign_key_name,
						OBJECT_NAME(f.parent_object_id) AS table_name
					FROM
						sys.foreign_keys AS f
					INNER JOIN
						sys.foreign_key_columns AS fc
					ON
						f.object_id = fc.constraint_object_id;
			""";

	public static String get() {
		return TEMPLATE;
	}

}
