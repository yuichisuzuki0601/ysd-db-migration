package jp.co.ysd.db_migration.sql_compiler;

import org.springframework.beans.factory.annotation.Autowired;

import jp.co.ysd.db_migration.DaoFactory;
import jp.co.ysd.db_migration.dao.Dao;

/**
 *
 * @author yuichi.suzuki
 *
 */
public abstract class Compile {

	@Autowired
	private DaoFactory factory;

	protected Dao getDao() {
		return factory.get();
	}

	public abstract String compile(String[] args) throws Exception;

}
