package jp.co.ysd.db_migration;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.Banner.Mode;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import jp.co.ysd.db_migration.manager.CommandManager;

/**
 *
 * @author yuichi
 *
 */
@SpringBootApplication
public class DbMigrationApplication {

	public static void main(String... args) throws Exception {
		_main("-mode", "dropall");
		_main("-rootdir", "../mast/database/mast-web-service", "-datadir", "data-prod");
		_main("-mode", "dataall", "-rootdir", "../mast/database/mast-web-service", "-datadir", "data-dev");
	}

	private static final Map<String, String> FIXED_PROPERTIES = new HashMap<>();
	static {
		FIXED_PROPERTIES.put("logging.level.org.springframework", "WARN");
		FIXED_PROPERTIES.put("spring.main.allowCircularReferences", "true");
	}

	public static void _main(String... args) throws Exception {
		FIXED_PROPERTIES.entrySet().stream().forEach(e -> System.setProperty(e.getKey(), e.getValue()));
		var cm = new CommandManager(args);
		var app = new SpringApplication(DbMigrationApplication.class);
		app.setBannerMode(Mode.OFF);
		app.setLogStartupInfo(false);
		app.run(args).getBean(DbMigrationService.class).execute(cm.getMode(), cm.getRootDir(), cm.getDataDir());
	}

}
