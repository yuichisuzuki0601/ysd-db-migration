package jp.co.ysd.db_migration;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.Banner.Mode;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

/**
 *
 * @author yuichi
 *
 */
@SpringBootApplication
public class DbMigrationApplication {

	private static ApplicationContext ctx;

	private static final Map<String, String> FIXED_PROPERTIES = new HashMap<>();
	static {
		// FIXED_PROPERTIES.put("logging.level.jp.co.ysd", "WARN");
		FIXED_PROPERTIES.put("logging.level.org.springframework", "WARN");
		FIXED_PROPERTIES.put("spring.main.allowCircularReferences", "true");
	}

	public static void main(String... args) throws Exception {
		FIXED_PROPERTIES.entrySet().stream().forEach(e -> System.setProperty(e.getKey(), e.getValue()));
		CommandManager.init(args);
		var app = new SpringApplication(DbMigrationApplication.class);
		app.setBannerMode(Mode.OFF);
		app.setLogStartupInfo(false);
		ctx = app.run(args);
		getBean(DbMigrationService.class).execute();
	}

	public static <T> T getBean(Class<T> clazz) {
		return ctx.getBean(clazz);
	}

}
