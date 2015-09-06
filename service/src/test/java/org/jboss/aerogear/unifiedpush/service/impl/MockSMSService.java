package org.jboss.aerogear.unifiedpush.service.impl;

import javax.ejb.Stateless;

import org.jboss.aerogear.unifiedpush.service.SMSService;

// TODO: decide on package placement
@Stateless
public class MockSMSService implements SMSService {
	
	public String message;

	@Override
	public void sendSMS(String phoneNumber, String message) {
		this.message = message;
	}
	
}
