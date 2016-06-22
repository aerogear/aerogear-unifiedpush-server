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
import org.apache.commons.lang.StringUtils;
import org.infinispan.manager.CacheContainer;
import org.jboss.aerogear.unifiedpush.api.Installation;
import org.jboss.aerogear.unifiedpush.api.Variant;
import org.jboss.aerogear.unifiedpush.dao.InstallationDao;
import org.jboss.aerogear.unifiedpush.service.Configuration;
import org.jboss.aerogear.unifiedpush.service.VerificationGatewayService;
import org.jboss.aerogear.unifiedpush.service.VerificationService;
import org.jboss.aerogear.unifiedpush.utils.AeroGearLogger;

@Singleton
@Startup
public class VerificationServiceImpl implements VerificationService {
	private final static int VERIFICATION_CODE_LENGTH = 5;
	private final AeroGearLogger logger = AeroGearLogger.getInstance(VerificationServiceImpl.class);

	private ConcurrentMap<Object, Set<Object>> deviceToToken;

	@Inject
	private VerificationGatewayService verificationService;
    @Inject
    private InstallationDao installationDao;
    @Inject
	private Configuration configuration;

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

		// Send verification messages only if variant name is not DEVNULL_NOTIFICATIONS_VARIANT
		if (!DEVNULL_NOTIFICATIONS_VARIANT.equalsIgnoreCase(variant.getName())) {
			verificationService.sendVerificationMessage(installation.getAlias(), verificationCode);
		}

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
	public VerificationResult verifyDevice(Installation installation, Variant variant, String verificationCode){
		final String key = buildKey(variant.getVariantID(), installation.getDeviceToken());
		Set<Object> codes = deviceToToken.get(key);

		// Support master code verification, should be used only for QA/Automation.
		String masterCode = configuration.getProperty(Configuration.PROP_MASTER_VERIFICATION);

		if (codes == null) {
			// Installation was already enabled
			if (installation.isEnabled()){
				return VerificationResult.SUCCESS;
			}

			logger.warning("Verification attempt was made without calling /registry/device, installation id: " + installation.getId());
			return VerificationResult.UNKNOWN;
		} else if (codes.contains(verificationCode) || (StringUtils.isNotEmpty(masterCode) && masterCode.equals(verificationCode))) {
			installation.setEnabled(true);
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
