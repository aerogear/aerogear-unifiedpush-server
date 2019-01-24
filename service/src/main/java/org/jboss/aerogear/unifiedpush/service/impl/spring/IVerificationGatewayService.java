package org.jboss.aerogear.unifiedpush.service.impl.spring;

import java.util.List;
import java.util.Locale;

import org.jboss.aerogear.unifiedpush.service.VerificationPublisher.MessageType;
import org.jboss.aerogear.unifiedpush.service.impl.spring.VerificationGatewayServiceImpl.VerificationPart;

/**
 * Service class used to send SMS messages.
 */
public interface IVerificationGatewayService {

	/**
	 * Sends a message to the specified alias
	 *
	 * @param pushApplicationId push application uuid
	 * @param alias             phone number / email to send to.
	 * @param message           text message to be sent
	 */
	void sendVerificationMessage(String pushApplicationId, String alias, MessageType reset, String code, Locale locale);

	List<VerificationPart> getChain();

	void initializeSender();
}
