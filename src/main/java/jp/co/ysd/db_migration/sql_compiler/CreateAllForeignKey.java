package jp.co.ysd.db_migration.sql_compiler;

import java.io.File;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jp.co.ysd.db_migration.util.FileAccessor;

/**
 *
 * @author yuichi.suzuki
 *
 */
@Component
public class CreateAllForeignKey extends Compile {

	@Autowired
	private CreateForeignKey createForeignKey;

	@Override
	public String compile(String[] args) throws Exception {
		File[] constraintFiles = FileAccessor.getConstraintFiles();
		StringBuilder result = new StringBuilder();
		for (File constraintFile : constraintFiles) {
			String tableName = FilenameUtils.removeExtension(constraintFile.getName()).replace("-constraint", "");
			result.append(createForeignKey.compile(new String[] { tableName }));
		}
		return result.toString();
	}

}
