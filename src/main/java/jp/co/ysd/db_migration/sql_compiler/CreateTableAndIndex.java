package jp.co.ysd.db_migration.sql_compiler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author yuichi
 *
 */
@Component
public class CreateTableAndIndex extends Compile {

	@Autowired
	private CreateTable createTable;

	@Autowired
	private CreateIndex createIndex;

	@Override
	public String compile(String[] args) throws Exception {
		return createTable.compile(args) + createIndex.compile(args);
	}

}
