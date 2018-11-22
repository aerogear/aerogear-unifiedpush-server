package org.jboss.aerogear.unifiedpush.service.sms;

import java.text.MessageFormat;
import java.util.Properties;

import org.apache.commons.lang3.ArrayUtils;
import org.jboss.aerogear.unifiedpush.api.verification.VerificationPublisher.MessageType;
import org.slf4j.Logger;

public abstract class AbstractSender {
	protected final static String DEFAULT_VERIFICATION_TEMPLATE = "{0}";

	protected String template;
	protected String templateReset;

	protected String getVerificationMessage(String code, MessageType type, String... additional) {
		if (type == MessageType.RESET) {
			return tlResetMessageFormat.get().format(ArrayUtils.addAll(new String[] { code }, additional));
		}
		return tlMessageFormat.get().format(ArrayUtils.addAll(new String[] { code }, additional));
	}

	private ThreadLocal<MessageFormat> tlMessageFormat = new ThreadLocal<MessageFormat>() {
		@Override
		public MessageFormat initialValue() {
			if (template == null || template.isEmpty()) {
				template = DEFAULT_VERIFICATION_TEMPLATE;
			}
			return new MessageFormat(template);
		}
	};

	private ThreadLocal<MessageFormat> tlResetMessageFormat = new ThreadLocal<MessageFormat>() {
		@Override
		public MessageFormat initialValue() {
			if (templateReset == null || templateReset.isEmpty()) {
				// Use default template if reset template is missing
				return new MessageFormat(template);
			}
			return new MessageFormat(templateReset);
		}
	};

	protected String getProperty(Properties properties, String key) {
		return getProperty(properties, key, null);
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
