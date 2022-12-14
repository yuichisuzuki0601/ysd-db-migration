package jp.co.ysd.db_migration.sql_compiler;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import jp.co.ysd.db_migration.util.FileAccessor;

/**
 *
 * @author yuichi
 *
 */
@Component
public class CreateIndex extends Compile {

	@Override
	@SuppressWarnings("unchecked")
	public String compile(String[] args) throws Exception {
		var tableName = args[0];
		var indexFile = FileAccessor.getIndexFile(tableName);
		if (indexFile.exists()) {
			Map<String, Object> index = new ObjectMapper().readValue(indexFile, Map.class);
			var cols = (List<Map<String, String>>) index.get("cols");
			return getDao().getCreateIndexSql(tableName, cols);
		} else {
			return "";
		}
	}

}
