package jp.co.ysd.db_migration.replacer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Types;

import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.stereotype.Component;

import jp.co.ysd.db_migration.util.FileAccessor;

/**
 *
 * @author yuichi
 *
 */
@Component
public class FileBlobReplacer implements DataReplacer {

	@Override
	public Object replace(Object original) {
		if (original instanceof String) {
			String str = original.toString();
			if (str.startsWith("file:")) {
				String filePath = FileAccessor.getDataDir() + "/" + str.replaceAll("file:", "");
				try {
					original = new SqlParameterValue(Types.BLOB, Files.readAllBytes(new File(filePath).toPath()));
				} catch (IOException e) {
					e.printStackTrace();
					System.exit(1);
				}
			}
		}
		return original;
	}

}
