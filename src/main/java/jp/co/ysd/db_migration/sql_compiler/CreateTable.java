package jp.co.ysd.db_migration.sql_compiler;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import jp.co.ysd.db_migration.util.FileAccessor;

/**
 *
 * @author yuichi.suzuki
 *
 */
@Component
public class CreateTable extends Compile {

	@Override
	@SuppressWarnings("unchecked")
	public String compile(String[] args) throws Exception {
		String tableName = args[0];
		File defineFile = FileAccessor.getDefineFile(tableName);
		Map<String, Object> define = new ObjectMapper().readValue(defineFile, Map.class);
		List<Map<String, String>> cols = (List<Map<String, String>>) define.get("cols");
		String pk = (String) define.get("pk");
		Object uq = define.get("uq");
		return getDao().getCreateTableSql(tableName, cols, pk, uq);
	}

}
