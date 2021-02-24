package jp.co.ysd.db_migration.replacer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.sqlserver.jdbc.StringUtils;

import jp.co.ysd.db_migration.util.CsvToJsonTranspiler;
import jp.co.ysd.db_migration.util.FileAccessor;
import jp.co.ysd.db_migration.util.SpaceFormatter;

/**
*
* @author yuichi
*
*/
@Component
public class JsonReplacer implements DataReplacer {

	private static final ObjectMapper OM = new ObjectMapper();

	@Autowired
	protected List<DataReplacer> replacers;

	@Override
	public Object replace(Object original) {
		if (original instanceof String) {
			String str = original.toString();
			if (str.startsWith("json:")) {
				String data = str.replaceAll("json:", "");
				try {
					String jsonStr = getFileJsonObject(data);
					jsonStr = getTargetJsonObject(data, jsonStr);
					jsonStr = filterProperties(data, jsonStr);
					original = SpaceFormatter.format(jsonStr);
				} catch (IOException e) {
					e.printStackTrace();
					System.exit(1);
				}
			}
		}
		return original;
	}

	private String getBeforeSpritter(String org, String... splitters) {
		int splitIndex = org.indexOf(splitters[0]);
		String result = org.substring(0, splitIndex > 0 ? splitIndex : org.length());
		String[] shiftedSplitters = Arrays.stream(splitters).skip(1).toArray(String[]::new);
		return shiftedSplitters.length == 0 ? result : getBeforeSpritter(result, shiftedSplitters);
	}

	private String getAfterSplitter(String org, String splitter) {
		int splitIndex = org.indexOf(splitter);
		return splitIndex > 0 ? org.substring(splitIndex + 1) : org;
	}

	private String getFileJsonObject(String data) throws IOException {
		String filePath = FileAccessor.getDataDir() + "/" + getBeforeSpritter(data, "#", "@");
		String extension = FilenameUtils.getExtension(filePath);
		if ("csv".equals(extension)) {
			return CsvToJsonTranspiler.transpile(filePath);
		} else if ("json".equals(extension)) {
			return new String(Files.readAllBytes(Paths.get(filePath)));
		}
		return "";
	}

	@SuppressWarnings("unchecked")
	private String getTargetJsonObject(String org, String jsonStr) throws IOException {
		Map<String, Object> json = null;
		if (jsonStr.startsWith("[")) {
			List<Object> l = OM.readValue(jsonStr, List.class);
			json = l.stream().collect(Collectors.toMap(o -> String.valueOf(l.indexOf(o)), o -> o));
		} else if (jsonStr.startsWith("{")) {
			json = OM.readValue(jsonStr, Map.class);
		}
		if (json == null) {
			return jsonStr;
		}
		for (DataReplacer replacer : replacers) {
			for (Entry<String, Object> e : json.entrySet()) {
				json.put(e.getKey(), replacer.replace(e.getValue()));
			}
		}
		if (!org.contains("#")) {
			return OM.writeValueAsString(json);
		}
		String dest = getAfterSplitter(org, "#");
		String result = OM.writeValueAsString(json.get(getBeforeSpritter(dest, "#", "@")));
		return getTargetJsonObject(dest, result);
	}

	@SuppressWarnings("unchecked")
	private String filterProperties(String data, String jsonStr) throws IOException {
		if (data.contains("@") && !StringUtils.isEmpty(jsonStr)) {
			Map<String, Object> json = OM.readValue(jsonStr, Map.class);
			List<String> targetProperties = Arrays.asList(getAfterSplitter(data, "@").split(","));
			for (String key : new HashSet<>(json.keySet())) {
				if (!targetProperties.contains(key)) {
					json.remove(key);
				}
			}
			return OM.writeValueAsString(json);
		} else {
			return jsonStr;
		}
	}

}
