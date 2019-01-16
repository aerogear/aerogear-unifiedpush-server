package org.jboss.aerogear.unifiedpush.service.sms;

import java.util.Locale;
import java.util.Properties;

import org.slf4j.Logger;
import org.springframework.context.MessageSource;

public abstract class AbstractSender {
	protected final static String DEFAULT_VERIFICATION_TEMPLATE = "{0}";

	protected String getProperty(Properties properties, String key) {
		return getProperty(properties, key, null);
	}

	protected String getMessage(MessageSource messageSource, String key, String locale, Object... attributes) {
		return messageSource.getMessage(key, attributes, Locale.forLanguageTag(locale));
	}

	protected String getMessage(MessageSource messageSource, String key, String locale) {
		return getMessage(messageSource, key, locale, new Object[] {});
	}

	protected String getProperty(Properties properties, String key, String defaultValue) {
		String value = properties.getProperty(key);
		if (value == null) {
			getLogger().warn("cannot find property " + key + " in configuration");
			return defaultValue;
		}
		return value;
	}

	protected abstract Logger getLogger();

}
