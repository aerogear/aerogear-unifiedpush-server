package org.jboss.aerogear.unifiedpush.service.impl;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.commons.lang.RandomStringUtils;
import org.infinispan.manager.CacheContainer;
import org.jboss.aerogear.unifiedpush.api.Installation;
import org.jboss.aerogear.unifiedpush.api.Variant;
import org.jboss.aerogear.unifiedpush.dao.InstallationDao;
import org.jboss.aerogear.unifiedpush.service.SMSService;
import org.jboss.aerogear.unifiedpush.service.VerificationService;
import org.jboss.aerogear.unifiedpush.utils.AeroGearLogger;

@Singleton
@Startup
public class VerificationServiceImpl implements VerificationService {
	private final static int VERIFICATION_CODE_LENGTH = 5;
	private final AeroGearLogger logger = AeroGearLogger.getInstance(VerificationServiceImpl.class);
	
	private ConcurrentMap<Object, Set<Object>> deviceToToken;
	
	@Inject
	private SMSService smsService;
    @Inject
    private InstallationDao installationDao;
	
	@PostConstruct
	private void startup() {
		CacheContainer container;
	    
		try {
			Context ctx = new InitialContext();
			container = (CacheContainer) ctx.lookup("java:jboss/infinispan/container/installation-verification");

			deviceToToken = container.getCache("verification");
		} catch (NamingException e) {
			logger.warning("Unable to locate infinispan cache installation-verification, rolling back to ConcurrentHashMap impl!");
			deviceToToken = new ConcurrentHashMap<>();
		}
	    
	}
	
	@Override
	public String retryDeviceVerification(String deviceToken, Variant variant) {
		Installation installation = installationDao.findInstallationForVariantByDeviceToken(variant.getVariantID(), deviceToken);
		return initiateDeviceVerification(installation, variant);
	}
	
	@Override
	public String initiateDeviceVerification(Installation installation, Variant variant) {
		// create a random string made up of numbers
		String verificationCode = RandomStringUtils.random(VERIFICATION_CODE_LENGTH, false, true);
		smsService.sendSMS(installation.getAlias(), verificationCode);
		String key = buildKey(variant.getVariantID(), installation.getDeviceToken());
		Set<Object> codes;
		
		if (!deviceToToken.containsKey(key)){
			codes = new HashSet<Object>();
		}else{
			codes = deviceToToken.get(key);
		}
	
		codes.add(verificationCode);
		deviceToToken.putIfAbsent(key, codes);
		
		return verificationCode;
	}

	@Override
	public VerificationResult verifyDevice(String variantID, String deviceToken, String verificationAttempt) {
		final String key = buildKey(variantID, deviceToken);
		Set<Object> codes = deviceToToken.get(key);
		if (codes == null) {
			return VerificationResult.UNKNOWN;
		} else if (codes.contains(verificationAttempt)) {
			Installation installation = installationDao.findInstallationForVariantByDeviceToken(variantID, deviceToken);
			installation.setEnabled(true);
			// TODO: there should be a "verifyDevice" like method in ClientInstallationService, which delegates here,
			// so implementations of VerificationService will not have to update the installation themselves.
			installationDao.update(installation);
			deviceToToken.remove(key);
			return VerificationResult.SUCCESS;
		}
		return VerificationResult.FAIL;
	}

	private String buildKey(String variantID, String deviceToken) {
		return variantID + "_" + deviceToken;
	}
}
