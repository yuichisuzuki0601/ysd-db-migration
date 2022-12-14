package jp.co.ysd.db_migration.replacer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jp.co.ysd.db_migration.DaoManager;
import jp.co.ysd.db_migration.dao.Dao;

// TODO 行指定とかに名前変えた方が良い
@Component
public class ForeignKeyReplacer implements DataReplacer {

	protected Logger l = LoggerFactory.getLogger(getClass());

	@Autowired
	private DaoManager factory;

	@Override
	public Object replace(Object original) {
		if (original instanceof String) {
			String str = original.toString();
			if (str.startsWith("foreignKey:")) {
				String[] info = str.replaceAll("foreignKey:", "").split(":");
				String tableName = info[0];
				String index = info[1];
				Dao dao = factory.get();
				try {
					Object o = dao.selectDataOrderById(tableName, "id").get(Integer.parseInt(index) - 1);
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