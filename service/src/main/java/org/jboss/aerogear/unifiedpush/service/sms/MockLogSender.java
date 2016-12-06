package org.jboss.aerogear.unifiedpush.service.sms;

import java.util.Properties;

import org.jboss.aerogear.unifiedpush.api.verification.VerificationPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sends SMS over Clickatell's HTTP API.
 */
public class MockLogSender implements VerificationPublisher {
	private static final Logger logger = LoggerFactory.getLogger(MockLogSender.class);

	@Override
	public void send(String alias, String message, Properties properties) {
		logger.info("Logging validation message: \"" + message + "\" for alias: " + alias);
	}

}
