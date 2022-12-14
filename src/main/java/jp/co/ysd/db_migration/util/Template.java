package jp.co.ysd.db_migration.util;

/**
 * 
 * @author yuichi
 *
 */
public class Template {

	private String template;

	public static Template of(String template) {
		return new Template(template);
	}

	private Template(String template) {
		this.template = template;
	}

	public String bind(Object... params) {
		var buf = template;
		for (var param : params) {
			buf = buf.replaceFirst("\\{.*?\\}", param.toString());
		}
		return buf;
	}

	@Override
	public String toString() {
		return template;
	}

}
