package jp.co.ysd.db_migration.replacer;

import java.util.UUID;

import org.springframework.stereotype.Component;

/**
 *
 * @author yuichi
 *
 */
@Component
public class UUIDReplacer implements DataReplacer {

	@Override
	public Object replace(Object original) {
		if (original instanceof String) {
			String str = original.toString();
			if (str.startsWith("uuid:")) {
				original = UUID.randomUUID().toString();
			}
		}
		return original;
	}

}
