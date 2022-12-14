package jp.co.ysd.db_migration.replacer;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 *
 * @author yuichi
 *
 */
@Component
public class CurrentTimestampReplacer implements DataReplacer, InitializingBean {

	private static final DateFormat DF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	@Value("${database.timezone:UTC}")
	private String timezone;

	@Override
	public void afterPropertiesSet() throws Exception {
		DF.setTimeZone(TimeZone.getTimeZone(timezone));
	}

	@Override
	public Object replace(Object original) {
		if (original instanceof String) {
			var str = original.toString();
			if (str.equals("CURRENT_TIMESTAMP")) {
				original = DF.format(new Date());
			}
		}
		return original;
	}

}
