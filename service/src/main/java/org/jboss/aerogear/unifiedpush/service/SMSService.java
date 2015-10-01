package org.jboss.aerogear.unifiedpush.service;


/**
 * Service class used to send SMS messages. 
 */
public interface SMSService {
	
	/**
	 * Sends a message to the specified number
	 * @param phoneNumber phone number to send to
	 * @param message text message to be sent
	 */
	void sendSMS(String phoneNumber, String message);
}
