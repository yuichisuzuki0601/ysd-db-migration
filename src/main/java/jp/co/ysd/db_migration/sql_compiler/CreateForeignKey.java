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
public class CreateForeignKey extends Compile {

	@Override
	@SuppressWarnings("unchecked")
	public String compile(String[] args) throws Exception {
		var tableName = args[0];
		var constraintFile = FileAccessor.getConstraintFile(tableName);
		if (constraintFile.exists()) {
			Map<String, Object> constraint = new ObjectMapper().readValue(constraintFile, Map.class);
			var cols = (List<Map<String, Object>>) constraint.get("cols");
			return getDao().getCreateForeignKeySql(tableName, cols);
		} else {
			return "";
		}
	}

}
