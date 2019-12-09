package jp.co.ysd.db_migration.replacer;

import java.io.File;
import java.io.IOException;
import java.net.URLConnection;
import java.nio.file.Files;
import java.util.Base64;

import org.springframework.stereotype.Component;

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
			String str = original.toString();
			if (str.startsWith("datauri:")) {
				String filePath = str.replaceAll("datauri:", "");
				try {
					File file = new File(filePath);
					String contentType = URLConnection.getFileNameMap().getContentTypeFor(file.getName());
					byte[] data = Files.readAllBytes(file.toPath());
					String base64str = Base64.getEncoder().encodeToString(data);
					StringBuilder sb = new StringBuilder();
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
