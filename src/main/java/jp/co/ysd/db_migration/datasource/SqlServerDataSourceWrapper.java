package jp.co.ysd.db_migration.datasource;

import org.springframework.util.StringUtils;

import jp.co.ysd.db_migration.CommandManager;
import jp.co.ysd.db_migration.properties.spring.datasource.DatasourceProperty;

public class SqlServerDataSourceWrapper extends DataSourceWrapper {

	protected SqlServerDataSourceWrapper(DatasourceProperty property) {
		super(property);
	}

	@Override
	public String getBaseUrl() {
		var spliteds = getProperty().getUrl().split(";");
		var result = spliteds[0];
		for (var i = 1; i < spliteds.length; ++i) {
			var splited = spliteds[i];
			if (!splited.contains("databaseName=")) {
				result += ";" + spliteds[i];
			}
		}
		return result;
	}

	@Override
	public String getSchemaSeparator() {
		return ";databaseName=";
	}

	@Override
	public String getSchema() {
		var specifiedTargetSchema = CommandManager.getInstance().getTargetSchema();
		if (StringUtils.hasText(specifiedTargetSchema)) {
			return specifiedTargetSchema;
		}
		var spliteds = getProperty().getUrl().split(";");
		for (var i = 1; i < spliteds.length; ++i) {
			var splited = spliteds[i];
			if (splited.contains("databaseName=")) {
				return splited.split("=")[1];
			}
		}
		return null;
	}

}
