package jp.co.ysd.db_migration.replacer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jp.co.ysd.db_migration.dao.DaoManager;

@Component
public class RowNumberReplacer implements DataReplacer {

	protected Logger l = LoggerFactory.getLogger(getClass());

	@Autowired
	private DaoManager daoManager;

	@Override
	public Object replace(Object original) {
		if (original instanceof String) {
			var str = original.toString();
			if (str.startsWith("rowNumber:")) {
				var info = str.replaceAll("rowNumber:", "").split(":");
				var schema = "";
				var tableName = info[0];
				if (tableName.contains(".")) {
					schema = tableName.split("\\.")[0];
					tableName = tableName.split("\\.")[1];
				}
				var index = info[1];
				var dao = daoManager.get();
				try {
					var o = dao.selectDataOrderById(schema, tableName, "id").get(Integer.parseInt(index) - 1);
					original = o.toString();
				} catch (IndexOutOfBoundsException e) {
					l.error("!!!IndexOutOfBounds!!! table:" + tableName);
					throw e;
				}
			}
		}
		return original;
	}

}
