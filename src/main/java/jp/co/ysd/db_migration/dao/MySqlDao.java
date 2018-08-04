package jp.co.ysd.db_migration.dao;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 *
 * @author yuichi.suzuki
 *
 */
@Component
public class MySqlDao extends Dao {

	private final static String SQL_EXIST_TBL = "SHOW TABLES FROM %s LIKE ?";
	private final static String SQL_SELECT_ALL_TBL = "SELECT TABLE_NAME AS name FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = '%s'";
	private final static String SQL_SELECT_FK = "SELECT f1.constraint_name AS foreign_key_name,f2.table_name AS table_name FROM information_schema.table_constraints f1 INNER JOIN information_schema.key_column_usage f2 ON f1.table_schema = f2.table_schema AND f1.constraint_name = f2.constraint_name WHERE f1.constraint_type = 'FOREIGN KEY'";
	private final static String SQL_DROP_FK = "ALTER TABLE %s DROP FOREIGN KEY %s";

	@Value("${spring.datasource.url}")
	private String url;

	private String getSchema() {
		int from = url.lastIndexOf("/") + 1;
		int to = url.indexOf("?");
		return url.substring(from, to);
	}

	@Override
	protected boolean existTable(String tableName) {
		return !j.query(String.format(SQL_EXIST_TBL, getSchema()), new Object[] { tableName },
				(rs, rowNum) -> rs.getObject(1)).isEmpty();
	}

	@Override
	protected String getSelectAllTableSql() {
		return String.format(SQL_SELECT_ALL_TBL, getSchema());
	}

	@Override
	protected String getSelectAllForeignKeySql() {
		return SQL_SELECT_FK;
	}

	@Override
	protected String getDropForeignKeySql() {
		return SQL_DROP_FK;
	}

}
