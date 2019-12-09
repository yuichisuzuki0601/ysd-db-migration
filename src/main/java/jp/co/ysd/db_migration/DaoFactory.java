package jp.co.ysd.db_migration;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jp.co.ysd.db_migration.dao.Dao;

/**
 *
 * @author yuichi
 *
 */
@Component
public class DaoFactory {

	private static final Map<String, String> DRIVER_DAOKEY_MAP = new HashMap<>();
	static {
		DRIVER_DAOKEY_MAP.put("com.microsoft.sqlserver.jdbc.SQLServerDriver", "sqlServerDao");
		DRIVER_DAOKEY_MAP.put("com.mysql.jdbc.Driver", "mySqlDao");
		DRIVER_DAOKEY_MAP.put("org.mariadb.jdbc.Driver", "mySqlDao");
	}

	@Value("${spring.datasource.driverClassName}")
	private String driverClassName;

	@Autowired
	private Map<String, Dao> daos;

	public Dao get() {
		return daos.get(DRIVER_DAOKEY_MAP.get(driverClassName));
	}

}
