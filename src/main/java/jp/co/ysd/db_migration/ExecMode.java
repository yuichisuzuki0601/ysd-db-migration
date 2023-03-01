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
	// dataall:データのみ全適用
	// replaceindex:インデックスの更新のみ
	// replaceindex:インデックスの削除のみ
	// replaceview:ビューの更新のみ
	APPLY, REBUILD, DROPALL, DATAALL, REPLACEINDEX, DROPINDEX, REPLACEVIEW;

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
