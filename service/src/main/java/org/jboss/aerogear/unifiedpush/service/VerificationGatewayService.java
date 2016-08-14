package org.jboss.aerogear.unifiedpush.service;


/**
 * Service class used to send SMS messages.
 */
public interface VerificationGatewayService {

	/**
	 * Sends a message to the specified alias
	 * @param alias phone number / email to send to.
	 * @param message text message to be sent
	 */
	void sendVerificationMessage(String alias, String message);
}
