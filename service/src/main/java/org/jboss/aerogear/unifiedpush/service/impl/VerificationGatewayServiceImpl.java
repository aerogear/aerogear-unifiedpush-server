package org.jboss.aerogear.unifiedpush.service.impl;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.jboss.aerogear.unifiedpush.api.verification.VerificationPublisher;
import org.jboss.aerogear.unifiedpush.service.Configuration;
import org.jboss.aerogear.unifiedpush.service.VerificationGatewayService;
import org.jboss.aerogear.unifiedpush.utils.AeroGearLogger;

/**
 * Default implementation of {@link VerificationGatewayService}. Note that this class does not implement the underlying
 * SMS sending mechanism. Rather, it uses an implementation of {@link VerificationPublisher} to do so.
 * 
 * @see VerificationPublisher
 */
@Singleton
public class VerificationGatewayServiceImpl implements VerificationGatewayService {
	
	private final AeroGearLogger logger = AeroGearLogger.getInstance(VerificationGatewayServiceImpl.class);

	private final static String VERIFICATION_IMPL_KEY = "aerogear.config.verification.impl.class";
	
	@Inject
	private Configuration configuration;
	
	private VerificationPublisher publisher;
	
	/**
	 * Initializes the SMS sender. We cache the sender since an implementation might set up
	 * its own (being, currently, outside of JBoss's resource management scope) resource upkeep.
	 */
	@PostConstruct
	public void initializeSender() {
		final String className = configuration.getProperty(VERIFICATION_IMPL_KEY);
		if (className == null) {
			logger.warning("cannot find sms sender implementation class");
			return;
		}
		
		try {
			publisher = (VerificationPublisher) Class.forName(className).newInstance();
		} catch (InstantiationException | IllegalAccessException
				| ClassNotFoundException e) {
			throw new RuntimeException("cannot instantiate class " + className);
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void sendVerificationMessage(String alias, String message) {
		if (publisher == null){
			// Retry initialization
			initializeSender();
		}
			
		publisher.send(alias, message, configuration.getProperties());
	}


}
