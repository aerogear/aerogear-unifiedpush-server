package org.jboss.aerogear.unifiedpush.service.sms;

import java.text.MessageFormat;
import java.util.Properties;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;

public abstract class AbstractSender  {
	protected final static String DEFAULT_VERIFICATION_TEMPLATE = "{0}";

	protected String template;

	protected String getVerificationMessage(String code, String ... additional) {
		return tlMessageFormat.get().format(ArrayUtils.addAll(new String[] {code}, additional));
	}

	protected ThreadLocal<MessageFormat> tlMessageFormat = new ThreadLocal<MessageFormat>() {
    	@Override
    	public MessageFormat initialValue() {
    		if (template == null || template.isEmpty()) {
    			template = DEFAULT_VERIFICATION_TEMPLATE;
    		}
			return new MessageFormat(template);
    	}
    };

	protected String getProperty(Properties properties, String key) {
		String value = properties.getProperty(key);
		if (value == null) {
			getLogger().warn("cannot find property " + key + " in configuration");
		}
		return value;
	}

	protected abstract Logger getLogger();

}
