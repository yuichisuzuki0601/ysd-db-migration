package jp.co.ysd.db_migration.dao;

import org.springframework.stereotype.Component;

import jp.co.ysd.db_migration.dao.sql.mysql.MySqlDropForeignKeySql;
import jp.co.ysd.db_migration.dao.sql.mysql.MySqlExistTableAndViewSql;
import jp.co.ysd.db_migration.dao.sql.mysql.MySqlSelectAllForeignKeySql;
import jp.co.ysd.db_migration.dao.sql.mysql.MySqlSelectAllIndexFromTableSql;
import jp.co.ysd.db_migration.dao.sql.mysql.MySqlSelectAllTableAndViewSql;

/**
 *
 * @author yuichi
 *
 */
@Component
public class MySqlDao extends Dao {

	private String getSchema() {
		return dataSourceWrapper.getSchema();
	}

	@Override
	protected String getSelectAllSchemasSql() {
		return "SHOW DATABASES";
	}

	@Override
	protected String getDropSchemaIfExistsSql() {
		return "DROP DATABASE IF EXISTS `" + getSchema() + "`";
	}

	@Override
	protected String getCreateSchemaIfNotExistsSql() {
		return "CREATE DATABASE IF NOT EXISTS `" + getSchema() + "`";
	}

	@Override
	protected boolean existTableAndView(String name) {
		return !j.query(MySqlExistTableAndViewSql.get(getSchema()), (rs, rowNum) -> rs.getObject(1), name).isEmpty();
	}

	@Override
	protected String getSelectAllTableAndViewSql() {
		return MySqlSelectAllTableAndViewSql.get(getSchema());
	}

	@Override
	protected String getSelectAllForeignKeySql() {
		return MySqlSelectAllForeignKeySql.get(getSchema());
	}

	@Override
	protected String getDropForeignKeySql(String tableName, String foreignKey) {
		return MySqlDropForeignKeySql.get(tableName, foreignKey);
	}

	@Override
	protected String getSelectAllIndexFromTableSql(String tableName) {
		return MySqlSelectAllIndexFromTableSql.get(tableName);
	}

}
