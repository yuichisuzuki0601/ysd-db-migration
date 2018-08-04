package jp.co.ysd.db_migration;

import java.util.Arrays;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.springframework.boot.Banner.Mode;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 *
 * @author yuichi.suzuki
 *
 */
@SpringBootApplication
public class DbMigrationApplication {

	public static void main(String[] args) {
		try {
			SpringApplication app = new SpringApplication(DbMigrationApplication.class);
			app.setBannerMode(Mode.OFF);
			app.setLogStartupInfo(false);
			System.setProperty("logging.level.org.springframework", "WARN");
			ConfigurableApplicationContext ctx = app.run(args);
			CommandLine cl = getCommandLine(args);
			ctx.getBean(DbMigrationService.class).execute(getMode(cl), getRootDir(cl));
		} catch (Throwable t) {
			t.printStackTrace();
			System.exit(1);
		}
		System.exit(0);
	}

	private static CommandLine getCommandLine(String[] orgArgs) throws ParseException {
		String[] args = Arrays.stream(orgArgs).filter(arg -> !arg.startsWith("--spring")).toArray(String[]::new);
		Options options = new Options();
		options.addOption("mode", true, "適用モードを指定します");
		options.addOption("rootdir", true, "テーブル定義ディレクトリ/データディレクトリのあるディレクトリパスを設定します");
		return new DefaultParser().parse(options, args, false);
	}

	private static ExecMode getMode(CommandLine cl) {
		return cl.hasOption("mode") ? ExecMode.of(cl.getOptionValue("mode")) : ExecMode.NORMAL;
	}

	private static String getRootDir(CommandLine cl) {
		return cl.hasOption("rootdir") ? cl.getOptionValue("rootdir") : null;
	}

}
