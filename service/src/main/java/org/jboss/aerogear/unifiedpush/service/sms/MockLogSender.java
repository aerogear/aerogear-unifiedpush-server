package org.jboss.aerogear.unifiedpush.service.sms;

import java.util.Properties;

import org.jboss.aerogear.unifiedpush.service.VerificationPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;

/**
 * Sends SMS over Clickatell's HTTP API.
 */
public class MockLogSender implements VerificationPublisher {
	private static final Logger logger = LoggerFactory.getLogger(MockLogSender.class);

	@Override
	public void send(String alias, String code, MessageType type, Properties properties, MessageSource messageSource,
			String locale) {
		logger.info("Logging validation message: \"" + code + "\" for alias: " + alias);
	}

}
