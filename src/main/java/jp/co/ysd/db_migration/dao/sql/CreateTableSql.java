package jp.co.ysd.db_migration.dao.sql;

import static jp.co.ysd.db_migration.util.SqlUtil.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import jp.co.ysd.db_migration.util.Template;

/**
 * 
 * @author yuichi
 *
 */
public final class CreateTableSql {

	private static final Template TEMPLATE = Template.of("CREATE TABLE `{tableName}` ({columnPart});");

	@SuppressWarnings("unchecked")
	public static String get(String tableName, List<Map<String, String>> columns, String pk, Object uq) {
		var columnPart = "";

		// column define part
		columnPart += columns.stream().map(col -> {
			var buf = new StringBuilder();
			buf.append(bq(col.get("name")) + " " + col.get("type"));
			var comment = col.get("comment");
			buf.append(comment != null ? " COMMENT '" + comment + "'" : "");
			return buf.toString();
		}).reduce((l, r) -> l + "," + r).get();

		// primary key part
		columnPart += pk != null ? ",PRIMARY KEY(`" + pk + "`)" : "";

		// unique part
		List<String> uqList = new ArrayList<>();
		if (uq instanceof List) {
			for (var uqStr : ((List<String>) uq)) {
				uqList.add(getUqLine(uqStr));
			}
		} else if (uq instanceof String) {
			uqList.add(getUqLine((String) uq));
		}
		for (var line : uqList) {
			columnPart += ",UNIQUE(" + line + ")";
		}

		return TEMPLATE.bind(tableName, columnPart);
	}

	private static String getUqLine(String uqStr) {
		return Arrays.stream(uqStr.split(",")).map(s -> bq(s.trim())).reduce((l, r) -> l + "," + r).get();
	}

}
