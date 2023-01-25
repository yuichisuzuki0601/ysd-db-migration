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
			var str = original.toString();
			if (str.startsWith("ulid:")) {
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
				}
				original = new ULID().nextULID();
			}
		}
		return original;
	}

}
