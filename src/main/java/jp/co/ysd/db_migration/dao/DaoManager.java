package jp.co.ysd.db_migration.dao;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jp.co.ysd.db_migration.properties.spring.datasource.DatasourceProperty;

/**
 *
 * @author yuichi
 *
 */
@Component
public class DaoManager {

	private static final Map<String, String> DRIVER_DAOKEY_MAP = new HashMap<>();
	static {
		DRIVER_DAOKEY_MAP.put("com.mysql.jdbc.Driver", "mySqlDao");
		DRIVER_DAOKEY_MAP.put("com.mysql.cj.jdbc.Driver", "mySqlDao");
		DRIVER_DAOKEY_MAP.put("org.mariadb.jdbc.Driver", "mySqlDao");
		DRIVER_DAOKEY_MAP.put("com.microsoft.sqlserver.jdbc.SQLServerDriver", "sqlServerDao");
	}

	@Autowired
	private DatasourceProperty property;

	@Autowired
	private Map<String, Dao> daos;

	public Dao get() {
		return daos.get(DRIVER_DAOKEY_MAP.get(property.getDriverClassName()));
	}

}
