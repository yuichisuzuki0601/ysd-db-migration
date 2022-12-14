package jp.co.ysd.db_migration.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;

/**
 *
 * @author yuichi
 *
 */
public final class CsvToJsonTranspiler {

	private static final ObjectMapper OM = new ObjectMapper();

	public static String transpile(String filePath) throws IOException {
		var result = new ArrayList<Map<String, Object>>();
		try (var br = Files.newBufferedReader(Paths.get(filePath))) {
			var mapper = new CsvMapper();
			var schema = mapper.schemaFor(Map.class).withHeader();
			MappingIterator<Map<String, Object>> it = mapper.readerFor(Map.class).with(schema).readValues(br);
			while (it.hasNext()) {
				var datum = new HashMap<String, Object>();
				for (var e : it.next().entrySet()) {
					var value = e.getValue();
					if (value instanceof String && "NULL".equals(value)) {
						value = null;
					}
					datum.put(e.getKey(), value);
				}
				result.add(datum);
			}
			return OM.writeValueAsString(result);
		}
	}

}
