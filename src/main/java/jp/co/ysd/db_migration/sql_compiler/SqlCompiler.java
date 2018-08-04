package jp.co.ysd.db_migration.sql_compiler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author yuichi.suzuki
 *
 */
@Component
public class SqlCompiler {

	@Autowired
	private Map<String, Compile> compiles;

	public String compile(String sql) throws Exception {
		List<Entry<String, Compile>> list = new ArrayList<>(compiles.entrySet());
		list.sort((l, r) -> r.getKey().compareTo(l.getKey()));
		for (Entry<String, Compile> e : list) {
			String functionSign = "@" + e.getKey();
			if (sql.startsWith(functionSign)) {
				String argsPart = sql.replace(functionSign, "").replaceFirst("^\\(", "").replaceFirst("\\)$", "");
				List<String> args = new ArrayList<>();
				for (String arg : argsPart.split(",")) {
					args.add(arg.trim().replace("\"", ""));
				}
				return e.getValue().compile(args.toArray(new String[0]));
			}
		}
		return sql + ";";
	}

}
