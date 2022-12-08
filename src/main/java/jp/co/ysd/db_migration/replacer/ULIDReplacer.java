package jp.co.ysd.db_migration.replacer;

import org.springframework.stereotype.Component;

import com.offbytwo.ulid.ULID;

/**
 *
 * @author yuichi
 *
 */
@Component
public class ULIDReplacer implements DataReplacer {

	@Override
	public Object replace(Object original) {
		if (original instanceof String) {
			String str = original.toString();
			if (str.startsWith("ulid:")) {
				original = new ULID().nextULID();
			}
		}
		return original;
	}

}
