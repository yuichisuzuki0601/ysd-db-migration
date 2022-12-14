package jp.co.ysd.db_migration.sql_compiler;

import org.springframework.beans.factory.annotation.Autowired;

import jp.co.ysd.db_migration.DaoManager;
import jp.co.ysd.db_migration.dao.Dao;

/**
 *
 * @author yuichi
 *
 */
public abstract class Compile {

	@Autowired
	private DaoManager factory;

	protected Dao getDao() {
		return factory.get();
	}

	public abstract String compile(String[] args) throws Exception;

}
