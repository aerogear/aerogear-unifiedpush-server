package org.jboss.aerogear.unifiedpush.service;

import java.util.Properties;

import org.slf4j.Logger;
import org.springframework.context.MessageSource;

/**
 * Implementation of this class are responsible for sending SMS messages.
 */
public interface VerificationPublisher {

	/**
	 * Sends the message to number.
	 * 
	 * @param phoneNumber number to send to
	 * @param code        OTP verification code.
	 * @param properties  any additional properties required to configure this sms
	 *                    sender
	 */
	void send(String phoneNumber, String code, MessageType type, Properties properties, MessageSource messageSource, String locale);

	/**
	 * Indicates rather next validation in chain should be validated.
	 */
	default boolean chain() {
		return true;
	};

	static void logError(Logger logger, String type, String hostname, String portnumb, String username, String password,
			String fromaddr, String toaddres, String subjectt, Exception e) {

		StringBuilder builder = new StringBuilder();
		builder.append("Cannot send " + type + " message using");
		builder.append(": hostname: ").append(hostname);
		builder.append(", portnumb: ").append(portnumb);
		builder.append(", username: ").append(username);
		builder.append(", password: ").append(password);
		builder.append(", fromaddr: ").append(fromaddr);
		builder.append(", toaddres: ").append(toaddres);

		logger.error(builder.toString(), e);
	}

	public enum MessageType {
		REGISTER, RESET
	}
}
