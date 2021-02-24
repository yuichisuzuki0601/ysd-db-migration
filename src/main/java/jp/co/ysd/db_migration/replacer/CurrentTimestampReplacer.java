package jp.co.ysd.db_migration.replacer;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.stereotype.Component;

/**
 *
 * @author yuichi
 *
 */
@Component
public class CurrentTimestampReplacer implements DataReplacer {

	private static final DateFormat DF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	@Override
	public Object replace(Object original) {
		if (original instanceof String) {
			String str = original.toString();
			if (str.equals("CURRENT_TIMESTAMP")) {
				original = DF.format(new Date());
			}
		}
		return original;
	}

}
