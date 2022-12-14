package jp.co.ysd.db_migration.replacer;

import java.io.File;
import java.io.IOException;
import java.net.URLConnection;
import java.nio.file.Files;
import java.util.Base64;

import org.springframework.stereotype.Component;

import jp.co.ysd.db_migration.util.FileAccessor;

/**
*
* @author yuichi
*
*/
@Component
public class DataUriReplacer implements DataReplacer {

	@Override
	public Object replace(Object original) {
		if (original instanceof String) {
			var str = original.toString();
			if (str.startsWith("datauri:")) {
				var filePath = FileAccessor.getDataDir() + "/" + str.replaceAll("datauri:", "");
				try {
					var file = new File(filePath);
					var contentType = URLConnection.getFileNameMap().getContentTypeFor(file.getName());
					var data = Files.readAllBytes(file.toPath());
					var base64str = Base64.getEncoder().encodeToString(data);
					var sb = new StringBuilder();
					sb.append("data:");
					sb.append(contentType);
					sb.append(";base64,");
					sb.append(base64str);
					original = sb.toString();
				} catch (IOException e) {
					e.printStackTrace();
					System.exit(1);
				}
			}
		}
		return original;
	}

}
