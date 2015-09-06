package org.jboss.aerogear.unifiedpush.service.impl;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Singleton;

import org.jboss.aerogear.unifiedpush.service.SMSService;

@Singleton
public class MockSMSService implements SMSService {
	
	public Map<String, String> phoneToMessage = new HashMap<>();

	@Override
	public void sendSMS(String phoneNumber, String message) {
		phoneToMessage.put(phoneNumber, message);
	}
	
}
