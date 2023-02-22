package jp.co.ysd.db_migration.datasource;

import org.springframework.util.StringUtils;

import jp.co.ysd.db_migration.manager.CommandManager;
import jp.co.ysd.db_migration.properties.spring.datasource.DatasourceProperty;

public class MySqlDataSourceWrapper extends DataSourceWrapper {

	protected MySqlDataSourceWrapper(DatasourceProperty property) {
		super(property);
	}

	@Override
	public String getBaseUrl() {
		var spliteds = getProperty().getUrl().split("/");
		return spliteds[0] + "/" + spliteds[1] + "/" + spliteds[2];
	}

	@Override
	public String getSchemaSeparator() {
		return "/";
	}

	@Override
	public String getSchema() {
		var specifiedTargetSchema = CommandManager.getInstance().getTargetSchema();
		if (StringUtils.hasText(specifiedTargetSchema)) {
			return specifiedTargetSchema;
		}
		var spliteds = getProperty().getUrl().split("/");
		if (spliteds.length >= 4) {
			return spliteds[3];
		}
		return null;
	}

}
