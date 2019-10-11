package jp.co.ysd.db_migration.replacer;

import org.springframework.stereotype.Component;

/**
 *
 * @author yuichi
 *
 */
@Component
public class BooleanReplacer implements DataReplacer {

	@Override
	public Object replace(Object original) {
		if (original instanceof String) {
			String str = original.toString();
			if (str.equals("false") || str.equals("FALSE")) {
				original = false;
			} else if (str.equals("true") || str.equals("TRUE")) {
				original = true;
			}
		}
		return original;
	}

}
