package org.jboss.aerogear.unifiedpush.service.impl;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang.RandomStringUtils;
import org.jboss.aerogear.unifiedpush.api.Installation;
import org.jboss.aerogear.unifiedpush.dao.InstallationDao;
import org.jboss.aerogear.unifiedpush.service.SMSService;
import org.jboss.aerogear.unifiedpush.service.VerificationService;

@Singleton
public class VerificationServiceImpl implements VerificationService {

	private final static int VERIFICATION_CODE_LENGTH = 5;
	
	private ConcurrentMap<String, String> deviceToToken = new ConcurrentHashMap<>();
	
	@Inject
	private SMSService smsService;
	
	@Inject
    private InstallationDao installationDao;
	
	@Override
	public String initiateDeviceVerification(Installation installation) {
		// create a random string made up of numbers
		String verificationCode = RandomStringUtils.random(VERIFICATION_CODE_LENGTH, false, true);
		smsService.sendSMS(installation.getAlias(), verificationCode);
		deviceToToken.put(installation.getDeviceToken(), verificationCode);
		return verificationCode;
	}

	@Override
	public VerificationResult verifyDevice(Installation installation, String verificationAttempt) {
		String code = deviceToToken.get(installation.getDeviceToken());
		if (code == null) {
			return VerificationResult.UNKNOWN;
		} else if (code.equals(verificationAttempt)) {
			installation.setEnabled(true);
			// TODO: there should be a "verifyDevice" like method in ClientInstallationService, which delegates here,
			// so implementations of VerificationService will not have to update the installation themselves.
			installationDao.update(installation);
			return VerificationResult.SUCCESS;
		}
		return VerificationResult.FAIL;
	}

}
