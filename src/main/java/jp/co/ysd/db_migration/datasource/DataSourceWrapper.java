package jp.co.ysd.db_migration.datasource;

import javax.sql.DataSource;

import org.springframework.boot.jdbc.DataSourceBuilder;

import jp.co.ysd.db_migration.properties.spring.datasource.DatasourceProperty;
import lombok.Data;

@Data
public abstract class DataSourceWrapper {

	private DatasourceProperty property;

	private DataSource dataSource;

	public DataSourceWrapper(DatasourceProperty property) {
		this.property = property;
		this.dataSource = DataSourceBuilder.create().//
				driverClassName(property.getDriverClassName()).//
				url(getUrl()).//
				username(property.getUsername()).//
				password(property.getPassword()).//
				build();
	}

	public abstract String getBaseUrl();

	public abstract String getSchemaSeparator();

	public abstract String getSchema();

	public String getUrl() {
		return getBaseUrl() + getSchemaSeparator() + getSchema();
	}
}
