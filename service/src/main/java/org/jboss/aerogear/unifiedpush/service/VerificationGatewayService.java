package org.jboss.aerogear.unifiedpush.service;

/**
 * Service class used to send SMS messages.
 */
public interface VerificationGatewayService {

	/**
	 * Sends a message to the specified alias
<<<<<<< 39b80a4e5cb8951d391af1d475d965bfd930ba33
	 * @param pushApplicationId push application id.
=======
	 * @param pushApplicationId push application uuid
>>>>>>> Fix java docs errors
	 * @param alias phone number / email to send to.
	 * @param message text message to be sent
	 */
	void sendVerificationMessage(String  pushApplicationId, String alias, String message);
}
