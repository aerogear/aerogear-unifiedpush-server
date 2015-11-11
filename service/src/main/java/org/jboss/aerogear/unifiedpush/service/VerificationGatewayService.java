package org.jboss.aerogear.unifiedpush.service;


/**
 * Service class used to send SMS messages. 
 */
public interface VerificationGatewayService {
	
	/**
	 * Sends a message to the specified alias
	 * @param phoneNumber phone number to send to
	 * @param message text message to be sent
	 */
	void sendVerificationMessage(String alias, String message);
}
