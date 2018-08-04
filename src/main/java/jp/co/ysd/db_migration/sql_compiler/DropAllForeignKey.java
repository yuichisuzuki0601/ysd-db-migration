package jp.co.ysd.db_migration.sql_compiler;

import org.springframework.stereotype.Component;

/**
 *
 * @author yuichi.suzuki
 *
 */
@Component
public class DropAllForeignKey extends Compile {

	@Override
	public String compile(String[] args) throws Exception {
		return getDao().getDropAllForeignKeySql();
	}

}
