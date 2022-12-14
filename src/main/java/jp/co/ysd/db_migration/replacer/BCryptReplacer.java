package jp.co.ysd.db_migration.replacer;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 *
 * @author yuichi
 *
 */
@Component
public class BCryptReplacer implements DataReplacer {

	@Override
	public Object replace(Object original) {
		if (original instanceof String) {
			var str = original.toString();
			if (str.startsWith("encrypt:")) {
				str = new BCryptPasswordEncoder().encode(str.replaceAll("encrypt:", ""));
				original = str;
			}
		}
		return original;
	}

}
