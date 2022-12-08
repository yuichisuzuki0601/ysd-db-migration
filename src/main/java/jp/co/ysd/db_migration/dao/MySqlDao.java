package jp.co.ysd.db_migration.dao;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 *
 * @author yuichi
 *
 */
@Component
public class MySqlDao extends Dao {

	private static final String SQL_EXIST_TBL_AND_VIEW = "SHOW TABLES FROM %s LIKE ?";
	private static final String SQL_SELECT_ALL_TBL_AND_VIEW = "SELECT TABLE_NAME AS name, TABLE_TYPE AS type FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = '%s'";
	private static final String SQL_SELECT_ALL_INDEX_FROM_TBL = "SHOW INDEX FROM %s WHERE Key_name LIKE 'idx_%%'";
	private static final String SQL_SELECT_FK = "SELECT f1.constraint_name AS foreign_key_name,f2.table_name AS table_name FROM information_schema.table_constraints f1 INNER JOIN information_schema.key_column_usage f2 ON f1.table_schema = f2.table_schema AND f1.constraint_name = f2.constraint_name WHERE f1.constraint_type = 'FOREIGN KEY' AND f2.constraint_schema = '%s'";
	private static final String SQL_DROP_FK = "ALTER TABLE %s DROP FOREIGN KEY %s";

	@Value("${spring.datasource.url}")
	private String url;

	private String getSchema() {
		int from = url.lastIndexOf("/") + 1;
		int to = url.indexOf("?") > 0 ? url.indexOf("?") : url.length();
		return url.substring(from, to);
	}

	@Override
	protected boolean existTableAndView(String name) {
		return !j.query(String.format(SQL_EXIST_TBL_AND_VIEW, getSchema()), (rs, rowNum) -> rs.getObject(1), name)
				.isEmpty();
	}

	@Override
	protected String getSelectAllTableAndViewSql() {
		return String.format(SQL_SELECT_ALL_TBL_AND_VIEW, getSchema());
	}

	@Override
	protected String getSelectAllIndexFromTableSql(String tableName) {
		return String.format(SQL_SELECT_ALL_INDEX_FROM_TBL, tableName);
	}

	@Override
	protected String getSelectAllForeignKeySql() {
		return String.format(SQL_SELECT_FK, getSchema());
	}

	@Override
	protected String getDropForeignKeySql() {
		return SQL_DROP_FK;
	}

}
