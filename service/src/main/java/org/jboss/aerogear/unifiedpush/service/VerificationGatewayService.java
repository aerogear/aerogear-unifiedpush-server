package org.jboss.aerogear.unifiedpush.service;

import org.jboss.aerogear.unifiedpush.api.Variant;

/**
 * Service class used to send SMS messages.
 */
public interface VerificationGatewayService {

	/**
	 * Sends a message to the specified alias
	 * @param alias phone number / email to send to.
	 * @param message text message to be sent
	 */
	void sendVerificationMessage(Variant variant, String alias, String message);
}
