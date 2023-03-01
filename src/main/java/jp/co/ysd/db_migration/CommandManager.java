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
public final class CommandManager {

	public static CommandManager instance;

	public static void init(String[] orgArgs) throws ParseException {
		instance = new CommandManager(orgArgs);
	}

	public static CommandManager getInstance() {
		return instance;
	}

	private CommandLine commandLine;

	private CommandManager(String[] orgArgs) throws ParseException {
		var args = Arrays.stream(orgArgs).filter(arg -> !arg.startsWith("--spring")).toArray(String[]::new);
		var options = new Options();
		options.addOption("mode", true, "適用モードを指定します");
		options.addOption("rootdir", true, "テーブル定義ディレクトリ/データディレクトリのあるディレクトリパスを設定します");
		options.addOption("datadir", true, "データディレクトリ名を設定します。デフォルトはdataです。");
		options.addOption("targetschema", true, "適用するスキーマを指定します");
		options.addOption("autoschema", false, "スキーマの自動生成削除機能を有効にします");
		this.commandLine = new DefaultParser().parse(options, args, false);
	}

	public ExecMode getMode() {
		return commandLine.hasOption("mode") ? ExecMode.of(commandLine.getOptionValue("mode")) : ExecMode.APPLY;
	}

	public String getRootDir() {
		return commandLine.hasOption("rootdir") ? commandLine.getOptionValue("rootdir") : null;
	}

	public String getDataDir() {
		return commandLine.hasOption("datadir") ? commandLine.getOptionValue("datadir") : null;
	}

	public String getTargetSchema() {
		return commandLine.hasOption("targetschema") ? commandLine.getOptionValue("targetschema") : null;
	}

	public boolean getAutoSchema() {
		return commandLine.hasOption("autoschema");
	}

}
