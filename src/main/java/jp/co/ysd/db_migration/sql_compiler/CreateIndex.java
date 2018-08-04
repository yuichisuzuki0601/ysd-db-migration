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
public class CreateIndex extends Compile {

	@Override
	@SuppressWarnings("unchecked")
	public String compile(String[] args) throws Exception {
		String tableName = args[0];
		File indexFile = FileAccessor.getIndexFile(tableName);
		if (indexFile.exists()) {
			Map<String, Object> index = new ObjectMapper().readValue(indexFile, Map.class);
			List<Map<String, Object>> cols = (List<Map<String, Object>>) index.get("cols");
			return getDao().getCreateIndexSql(tableName, cols);
		} else {
			return "";
		}
	}

}
