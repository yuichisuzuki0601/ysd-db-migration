package jp.co.ysd.db_migration;

import java.util.Arrays;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * 
 * @author yuichi
 *
 */
public class CommandManager {

	private CommandLine commandLine;

	public CommandManager(String[] orgArgs) throws ParseException {
		var args = Arrays.stream(orgArgs).filter(arg -> !arg.startsWith("--spring")).toArray(String[]::new);
		var options = new Options();
		options.addOption("mode", true, "適用モードを指定します");
		options.addOption("rootdir", true, "テーブル定義ディレクトリ/データディレクトリのあるディレクトリパスを設定します");
		options.addOption("datadir", true, "データディレクトリ名を設定します。デフォルトはdataです。");
		this.commandLine = new DefaultParser().parse(options, args, false);
	}

	public ExecMode getMode() {
		return commandLine.hasOption("mode") ? ExecMode.of(commandLine.getOptionValue("mode")) : ExecMode.NORMAL;
	}

	public String getRootDir() {
		return commandLine.hasOption("rootdir") ? commandLine.getOptionValue("rootdir") : null;
	}

	public String getDataDir() {
		return commandLine.hasOption("datadir") ? commandLine.getOptionValue("datadir") : null;
	}

}
