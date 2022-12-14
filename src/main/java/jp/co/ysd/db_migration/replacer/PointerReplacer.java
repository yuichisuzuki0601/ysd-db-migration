package jp.co.ysd.db_migration.replacer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
			var str = original.toString();
			if (str.startsWith("pointer:")) {
				var info = str.replaceAll("pointer:", "").split(":");
				var tableName = info[0];
				var id = info[1];
				var colmnName = info[2];
				var dao = daoManager.get();
				var o = dao.selectDataById(tableName, colmnName, id);
				original = o.toString();
			}
		}
		return original;
	}

}
