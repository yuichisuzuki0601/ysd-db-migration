package jp.co.ysd.db_migration.replacer;

import java.util.Date;

import org.springframework.stereotype.Component;

/**
 *
 * @author yuichi.suzuki
 *
 */
@Component
public class CurrentTimestampReplacer implements DataReplacer {

	@Override
	public Object replace(Object original) {
		if (original instanceof String) {
			String str = original.toString();
			if (str.equals("CURRENT_TIMESTAMP")) {
				original = new Date();
			}
		}
		return original;
	}

}
