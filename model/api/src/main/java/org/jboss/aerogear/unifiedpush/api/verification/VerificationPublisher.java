package org.jboss.aerogear.unifiedpush.api.verification;

import java.util.Properties;

/**
 * Implementation of this class are responsible for sending SMS messages.
 */
public interface VerificationPublisher {
	
	/**
	 * Sends the message to number.
	 * @param phoneNumber number to send to
	 * @param message text message
	 * @param properties any additional properties required to configure this sms sender
	 */
	void send(String phoneNumber, String message, Properties properties);
}
