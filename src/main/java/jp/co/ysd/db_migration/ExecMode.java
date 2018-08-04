package jp.co.ysd.db_migration;

/**
 *
 * @author yuichi.suzuki
 *
 */
public enum ExecMode {

	// なし or normal:作成されていないテーブルのみ適用
	// rebuild:全テーブル再作成
	// dropall:全テーブル削除
	// dataall:データのみ全適用
	NORMAL, REBUILD, DROPALL, DATAALL;

	public static ExecMode of(String mode) {
		for (ExecMode self : values()) {
			if (self.name().toLowerCase().equals(mode)) {
				return self;
			}
		}
		throw new IllegalArgumentException("no such enum object for the mode: " + mode);
	}

}
