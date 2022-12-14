package jp.co.ysd.db_migration.sql_compiler;

import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author yuichi
 *
 */
@Component
public class SqlCompiler {

	@Autowired
	private Map<String, Compile> compiles;

	public String compile(String sql) throws Exception {
		var list = new ArrayList<Entry<String, Compile>>(compiles.entrySet());
		list.sort((l, r) -> r.getKey().compareTo(l.getKey()));
		for (var e : list) {
			var functionSign = "@" + e.getKey();
			if (sql.startsWith(functionSign)) {
				var argsPart = sql.replace(functionSign, "").replaceFirst("^\\(", "").replaceFirst("\\)$", "");
				var args = new ArrayList<String>();
				for (var arg : argsPart.split(",")) {
					args.add(arg.trim().replace("\"", ""));
				}
				return e.getValue().compile(args.toArray(new String[0]));
			}
		}
		return sql + ";";
	}

}
