package org.jboss.aerogear.unifiedpush.service.impl;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.jboss.aerogear.unifiedpush.api.sms.SMSSender;
import org.jboss.aerogear.unifiedpush.service.Configuration;
import org.jboss.aerogear.unifiedpush.service.SMSService;

/**
 * Default implementation of {@link SMSService}. Note that this class does not implement the underlying
 * SMS sending mechanism. Rather, it uses an implementation of {@link SMSSender} to do so.
 * 
 * @see SMSSender
 */
@Singleton
public class SMSServiceImpl implements SMSService {

	private final static String SMS_IMPL_KEY = "aerogear.config.sms.impl.class";
	
	@Inject
	private Configuration configuration;
	
	private SMSSender sender;
	
	/**
	 * Initializes the SMS sender. We cache the sender since an implementation might set up
	 * its own (being, currently, outside of JBoss's resource management scope) resource upkeep.
	 */
	@PostConstruct
	public void initializeSender() {
		final String className = configuration.getProperty(SMS_IMPL_KEY);
		if (className == null) {
			throw new RuntimeException("cannot find sms sender implementation class");
		}
		
		try {
			sender = (SMSSender) Class.forName(className).newInstance();
		} catch (InstantiationException | IllegalAccessException
				| ClassNotFoundException e) {
			throw new RuntimeException("cannot instantiate class " + className);
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void sendSMS(String phoneNumber, String message) {
		sender.send(phoneNumber, message, configuration.getProperties());
	}


}
