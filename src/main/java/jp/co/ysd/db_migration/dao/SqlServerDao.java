package jp.co.ysd.db_migration.dao;

import org.springframework.stereotype.Component;

import jp.co.ysd.db_migration.dao.sql.sqlserver.SqlServerDropForeignKeySql;
import jp.co.ysd.db_migration.dao.sql.sqlserver.SqlServerExistTableAndViewSql;
import jp.co.ysd.db_migration.dao.sql.sqlserver.SqlServerSelectAllForeignKeySql;
import jp.co.ysd.db_migration.dao.sql.sqlserver.SqlServerSelectAllIndexFromTableSql;
import jp.co.ysd.db_migration.dao.sql.sqlserver.SqlServerSelectAllTableAndViewSql;

/**
 * 
 * @author yuichi
 *
 */
@Component
public class SqlServerDao extends Dao {

	@Override
	protected String getDropSchemaIfExistsSql() {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	protected String getCreateSchemaIfNotExistsSql() {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	protected boolean existTableAndView(String name) {
		return !j.query(SqlServerExistTableAndViewSql.get(), (rs, rowNum) -> rs.getObject(1), name).isEmpty();
	}

	@Override
	protected String getSelectAllTableAndViewSql() {
		return SqlServerSelectAllTableAndViewSql.get();
	}

	@Override
	protected String getSelectAllForeignKeySql() {
		return SqlServerSelectAllForeignKeySql.get();
	}

	@Override
	protected String getDropForeignKeySql(String tableName, String foreignKey) {
		return SqlServerDropForeignKeySql.get(tableName, foreignKey);
	}

	@Override
	protected String getSelectAllIndexFromTableSql(String tableName) {
		return SqlServerSelectAllIndexFromTableSql.get(tableName);
	}

}
