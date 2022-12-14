package jp.co.ysd.db_migration.sql_compiler;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import jp.co.ysd.db_migration.dao.sql.CreateTableSql;
import jp.co.ysd.db_migration.util.FileAccessor;

/**
 *
 * @author yuichi
 *
 */
@Component
public class CreateTable extends Compile {

	@Override
	@SuppressWarnings("unchecked")
	public String compile(String[] args) throws Exception {
		var tableName = args[0];
		var defineFile = FileAccessor.getDefineFile(tableName);
		var define = new ObjectMapper().readValue(defineFile, Map.class);
		var columns = (List<Map<String, String>>) define.get("cols");
		var pk = (String) define.get("pk");
		var uq = define.get("uq");
		return CreateTableSql.get(tableName, columns, pk, uq);
	}

}
