package org.jboss.aerogear.unifiedpush.api.verification;

import java.util.Properties;

import org.slf4j.Logger;

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
	void send(String phoneNumber, String code, Properties properties);

	/**
	 * Indicates rather next validation in chain should be validated.
	 */
	default boolean chain() {return true;};

	static void logError(Logger logger, String type, String hostname, String portnumb, String username, String password, String fromaddr,
			String toaddres, String subjectt, Exception e) {

		StringBuilder builder = new StringBuilder();
		builder.append("Cannot send SMS message using");
		builder.append(": hostname: ").append(hostname);
		builder.append(", portnumb: ").append(portnumb);
		builder.append(", username: ").append(username);
		builder.append(", password: ").append(password);
		builder.append(", fromaddr: ").append(fromaddr);
		builder.append(", toaddres: ").append(toaddres);

		logger.error(builder.toString() , e);
	}
}
