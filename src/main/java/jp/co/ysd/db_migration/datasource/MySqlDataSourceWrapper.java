package jp.co.ysd.db_migration.datasource;

import org.springframework.util.StringUtils;

import jp.co.ysd.db_migration.CommandManager;
import jp.co.ysd.db_migration.properties.spring.datasource.DatasourceProperty;

public class MySqlDataSourceWrapper extends DataSourceWrapper {

	protected MySqlDataSourceWrapper(DatasourceProperty property) {
		super(property);
	}

	@Override
	public String getBaseUrl() {
		var url = getProperty().getUrl();
		var splitedWithQ = url.split("\\?");
		var spliteds = splitedWithQ[0].split("/");
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

	@Override
	public String getAdditionalParameter() {
		var url = getProperty().getUrl();
		var splitedWithQ = url.split("\\?");
		if (splitedWithQ.length > 1) {
			return "?" + splitedWithQ[1];
		} else {
			return "";
		}
	}

}
