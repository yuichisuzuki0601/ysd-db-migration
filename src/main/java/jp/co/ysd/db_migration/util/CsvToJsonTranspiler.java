package jp.co.ysd.db_migration.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.LinkedList;
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

	public static String transpile(String filePath) throws IOException {
		var result = new LinkedList<Map<String, Object>>();
		try (var br = Files.newBufferedReader(Paths.get(filePath))) {
			var mapper = new CsvMapper();
			var schema = mapper.schemaFor(Map.class).withHeader();
			MappingIterator<Map<String, Object>> it = mapper.readerFor(Map.class).with(schema).readValues(br);
			while (it.hasNext()) {
				var datum = new LinkedHashMap<String, Object>();
				for (var e : it.next().entrySet()) {
					var value = e.getValue();
					if (value instanceof String && "NULL".equals(value)) {
						value = null;
					}
					datum.put(e.getKey(), value);
				}
				result.add(datum);
			}
			return new ObjectMapper().writeValueAsString(result);
		}
	}

}
