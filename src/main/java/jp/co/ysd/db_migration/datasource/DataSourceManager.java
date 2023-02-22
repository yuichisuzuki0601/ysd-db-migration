package jp.co.ysd.db_migration.datasource;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import jp.co.ysd.db_migration.properties.spring.datasource.DatasourceProperty;

@Component
@Configuration
public class DataSourceManager {

	private static final Map<String, Class<? extends DataSourceWrapper>> DRIVER_WRAPPER_MAP = new HashMap<>();
	static {
		DRIVER_WRAPPER_MAP.put("com.mysql.jdbc.Driver", MySqlDataSourceWrapper.class);
		DRIVER_WRAPPER_MAP.put("com.mysql.cj.jdbc.Driver", MySqlDataSourceWrapper.class);
		DRIVER_WRAPPER_MAP.put("org.mariadb.jdbc.Driver", MySqlDataSourceWrapper.class);
		DRIVER_WRAPPER_MAP.put("com.microsoft.sqlserver.jdbc.SQLServerDriver", SqlServerDataSourceWrapper.class);
	}

	@Autowired
	private DatasourceProperty property;

	@Bean
	public DataSourceWrapper dataSourceWrapper() {
		var clazz = DRIVER_WRAPPER_MAP.get(property.getDriverClassName());
		try {
			var result = clazz.getDeclaredConstructor(DatasourceProperty.class).newInstance(property);
			if (result.getSchema() == null) {
				throw new RuntimeException("schema is required.");
			}
			return result;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Bean
	public DataSource dataSource() {
		return dataSourceWrapper().getDataSource();
	}

}
