package jp.co.ysd.db_migration.replacer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jp.co.ysd.db_migration.dao.Dao;
import jp.co.ysd.db_migration.manager.DaoManager;

/**
 *
 * @author yuichi
 *
 */
@Component
public class PointerReplacer implements DataReplacer {

	@Autowired
	private DaoManager daoManager;

	@Override
	public Object replace(Object original) {
		if (original instanceof String) {
			String str = original.toString();
			if (str.startsWith("pointer:")) {
				String[] info = str.replaceAll("pointer:", "").split(":");
				String tableName = info[0];
				String id = info[1];
				String colmnName = info[2];
				Dao dao = daoManager.get();
				Object o = dao.selectDataById(tableName, colmnName, id);
				original = o.toString();
			}
		}
		return original;
	}

}
