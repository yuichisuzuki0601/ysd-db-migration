package jp.co.ysd.db_migration.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

/**
 *
 * @author yuichi
 *
 */
public final class CsvToJsonTranspiler {

	private static final ObjectMapper OM = new ObjectMapper();

	public static String transpile(String filePath) throws IOException {
		List<Map<String, Object>> result = new ArrayList<>();
		try (BufferedReader br = Files.newBufferedReader(Paths.get(filePath))) {
			CsvMapper mapper = new CsvMapper();
			CsvSchema schema = mapper.schemaFor(Map.class).withHeader();
			MappingIterator<Map<String, Object>> it = mapper.readerFor(Map.class).with(schema).readValues(br);
			while (it.hasNext()) {
				Map<String, Object> datum = new HashMap<>();
				for (Entry<String, Object> e : it.next().entrySet()) {
					Object value = e.getValue();
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
