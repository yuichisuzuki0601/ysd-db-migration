package jp.co.ysd.db_migration.sql_compiler;

import org.springframework.beans.factory.annotation.Autowired;

import jp.co.ysd.db_migration.dao.Dao;
import jp.co.ysd.db_migration.dao.DaoManager;

/**
 *
 * @author yuichi
 *
 */
public abstract class Compile {

	@Autowired
	private DaoManager daoManager;

	protected Dao getDao() {
		return daoManager.get();
	}

	public abstract String compile(String[] args) throws Exception;

}
