package jp.co.ysd.db_migration.replacer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jp.co.ysd.db_migration.DaoFactory;
import jp.co.ysd.db_migration.dao.Dao;

/**
 *
 * @author yuichi
 *
 */
@Component
public class PointerReplacer implements DataReplacer {

	@Autowired
	private DaoFactory factory;

	@Override
	public Object replace(Object original) {
		if (original instanceof String) {
			String str = original.toString();
			if (str.startsWith("pointer:")) {
				String[] info = str.replaceAll("pointer:", "").split(":");
				String tableName = info[0];
				String id = info[1];
				String colmnName = info[2];
				Dao dao = factory.get();
				Object o = dao.selectDataById(tableName, colmnName, id);
				original = o.toString();
			}
		}
		return original;
	}

}
