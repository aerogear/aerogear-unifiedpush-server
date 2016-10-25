package org.jboss.aerogear.unifiedpush.service.sms;

import java.text.MessageFormat;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractEmailSender {
	private final Logger logger = LoggerFactory.getLogger(AbstractEmailSender.class);

	protected final static String DEFAULT_VERIFICATION_TEMPLATE = "{0}";

	protected final static String HOSTNAME_KEY = "aerogear.config.email.sender.hostname";
	protected final static String PORTNUMB_KEY = "aerogear.config.email.sender.portnumber";
	protected final static String USERNAME_KEY = "aerogear.config.email.sender.username";
	protected final static String PASSWORD_KEY = "aerogear.config.email.sender.password";
	protected final static String FROMADDR_KEY = "aerogear.config.email.sender.fromaddress";
	protected final static String SUBJECTT_KEY = "aerogear.config.email.sender.subject";

	protected final static String MESSAGE_TMPL = "aerogear.config.email.sender.template";

	protected String template;

	protected String getProperty(Properties properties, String key) {
		String value = properties.getProperty(key);
		if (value == null) {
			logger.warn("cannot find property " + key + " in configuration");
		}
		return value;
	}

	protected String getVerificationTemplate(String verificationCode) {
		return tlMessageFormat.get().format(new Object[] { verificationCode });
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
}
