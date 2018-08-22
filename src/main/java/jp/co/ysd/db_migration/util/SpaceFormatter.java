package jp.co.ysd.db_migration.util;

/**
 *
 * @author yuichi
 *
 */
public final class SpaceFormatter {

	private SpaceFormatter() {
	}

	/**
	 * 改行、タブ、連続スペースなどを全て半角スペースに変換します
	 *
	 * @param org
	 * @return
	 */
	public static String format(String org) {
		String dest = org.replaceAll("[\\r|\\n|\\\t]", " ");
		dest = dest.trim();
		dest = dest.replaceAll(" {2,}", " ");
		return dest;
	}

}
