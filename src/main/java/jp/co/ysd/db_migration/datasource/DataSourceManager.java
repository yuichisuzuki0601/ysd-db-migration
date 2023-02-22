package jp.co.ysd.db_migration.datasource;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.stereotype.Component;

import jp.co.ysd.db_migration.properties.spring.datasource.DatasourceProperty;

@Component
@Configuration
public class DataSourceManager {

	public static enum DataSourceType {
		ROOT, TARGET
	}

	public static class CurrentDataSource {

		private static CurrentDataSource instance;

		public static CurrentDataSource getInstance() {
			if (instance == null) {
				instance = new CurrentDataSource();
			}
			return instance;
		}

		private DataSourceType dataSourceType = DataSourceType.TARGET;

		public DataSourceType getDataSourceType() {
			return dataSourceType;
		}

		public void toRoot() {
			this.dataSourceType = DataSourceType.ROOT;
		}

		public void toTarget() {
			this.dataSourceType = DataSourceType.TARGET;
		}
	}

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
	public DataSource dataSource() {
		var wrapper = dataSourceWrapper();

		var resolver = new AbstractRoutingDataSource() {
			@Override
			protected Object determineCurrentLookupKey() {
				return CurrentDataSource.getInstance().getDataSourceType();
			}
		};

		var root = wrapper.getRootDataSource();
		var target = wrapper.getTargetDataSource();

		resolver.setDefaultTargetDataSource(target);

		var dataSources = new HashMap<Object, Object>();
		dataSources.put(DataSourceType.ROOT, root);
		dataSources.put(DataSourceType.TARGET, target);
		resolver.setTargetDataSources(dataSources);

		return resolver;
	}

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

}
