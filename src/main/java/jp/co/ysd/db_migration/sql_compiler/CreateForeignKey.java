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
public class CreateForeignKey extends Compile {

	@Override
	@SuppressWarnings("unchecked")
	public String compile(String[] args) throws Exception {
		String tableName = args[0];
		File constraintFile = FileAccessor.getConstraintFile(tableName);
		if (constraintFile.exists()) {
			Map<String, Object> constraint = new ObjectMapper().readValue(constraintFile, Map.class);
			List<Map<String, Object>> cols = (List<Map<String, Object>>) constraint.get("cols");
			return getDao().getCreateForeignKeySql(tableName, cols);
		} else {
			return "";
		}
	}

}
