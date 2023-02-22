package jp.co.ysd.db_migration.properties.spring.datasource;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Component
@ConfigurationProperties(prefix = "spring.datasource")
@Data
public class DatasourceProperty {

	private String driverClassName;
	private String url;
	private String username;
	private String password;

}
