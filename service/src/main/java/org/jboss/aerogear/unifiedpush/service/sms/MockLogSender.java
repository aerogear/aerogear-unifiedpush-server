package org.jboss.aerogear.unifiedpush.service.sms;

import java.util.Properties;
import java.util.logging.Logger;

import org.jboss.aerogear.unifiedpush.api.verification.VerificationPublisher;

/**
 * Sends SMS over Clickatell's HTTP API.
 */
public class MockLogSender implements VerificationPublisher {
	private static final Logger logger = Logger.getLogger(MockLogSender.class.getName());

	@Override
	public void send(String alias, String message, Properties properties) {
		logger.info("Logging validation message: \"" + message + "\" for alias: ");
	}

}
