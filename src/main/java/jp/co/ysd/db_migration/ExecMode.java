package jp.co.ysd.db_migration;

import java.util.Arrays;

/**
 *
 * @author yuichi
 *
 */
public enum ExecMode {

	// なし or apply:作成されていないテーブルのみ適用
	// rebuild:全テーブル再作成
	// dropall:全テーブル削除
	// defineall:定義のみ全適用
	// dataall:データのみ全適用
	// replaceindex:インデックスの更新のみ
	// replaceindex:インデックスの削除のみ
	// replaceview:ビューの更新のみ
	// dropschema:スキーマ削除
	APPLY, REBUILD, DROPALL, DEFINEALL, DATAALL, REPLACEINDEX, DROPINDEX, REPLACEVIEW, DROPSCHEMA;

	public static ExecMode of(String mode) {
		for (ExecMode self : values()) {
			if (self.name().toLowerCase().equals(mode)) {
				return self;
			}
		}
		throw new IllegalArgumentException("no such enum object for the mode: " + mode);
	}

	public boolean is(ExecMode mode) {
		return mode == this;
	}

	public boolean not(ExecMode mode) {
		return mode != this;
	}

	public boolean some(ExecMode... modes) {
		return Arrays.stream(modes).anyMatch(this::is);
	}

}
