package jp.co.ysd.db_migration.dao;

import org.springframework.stereotype.Component;

/**
 * 
 * @author yuichi
 *
 */
@Component
public class SqlServerDao extends Dao {

	private static final String SQL_EXIST_TBL = "SELECT 1 FROM Sys.Tables WHERE name = ?";
	private static final String SQL_SELECT_ALL_TBL = "SELECT name FROM sys.objects WHERE type = 'U'";
	private static final String SQL_SELECT_FK = "SELECT f.name AS foreign_key_name, OBJECT_NAME(f.parent_object_id) AS table_name FROM sys.foreign_keys AS f INNER JOIN sys.foreign_key_columns AS fc ON f.object_id = fc.constraint_object_id;";
	private static final String SQL_DROP_FK = "ALTER TABLE %s DROP CONSTRAINT %s";

	@Override
	protected boolean existTable(String tableName) {
		return !j.query(SQL_EXIST_TBL, new Object[] { tableName }, (rs, rowNum) -> rs.getObject(1)).isEmpty();
	}

	@Override
	protected String getSelectAllTableSql() {
		return SQL_SELECT_ALL_TBL;
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
