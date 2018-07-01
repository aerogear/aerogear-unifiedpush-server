package org.jboss.aerogear.unifiedpush.service.impl;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.infinispan.manager.CacheContainer;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.remoting.transport.Transport;
import org.jboss.aerogear.unifiedpush.api.Alias;
import org.jboss.aerogear.unifiedpush.api.Installation;
import org.jboss.aerogear.unifiedpush.api.InstallationVerificationAttempt;
import org.jboss.aerogear.unifiedpush.api.Variant;
import org.jboss.aerogear.unifiedpush.cassandra.dao.model.OtpCode;
import org.jboss.aerogear.unifiedpush.cassandra.dao.model.OtpCodeKey;
import org.jboss.aerogear.unifiedpush.dao.InstallationDao;
import org.jboss.aerogear.unifiedpush.service.AliasService;
import org.jboss.aerogear.unifiedpush.service.VerificationService;
import org.jboss.aerogear.unifiedpush.service.impl.spring.IConfigurationService;
import org.jboss.aerogear.unifiedpush.service.impl.spring.IKeycloakService;
import org.jboss.aerogear.unifiedpush.service.impl.spring.IVerificationGatewayService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class VerificationServiceImpl implements VerificationService {
	private final static int VERIFICATION_CODE_LENGTH = 5;
	private final Logger logger = LoggerFactory.getLogger(VerificationServiceImpl.class);

	private ConcurrentMap<OtpCodeKey, Set<Object>> deviceToToken;

	@Inject
	private IConfigurationService configuration;
	@Inject
	private IVerificationGatewayService verificationService;
	@Inject
	private IKeycloakService keycloakService;
	@Inject
	private InstallationDao installationDao;
	@Inject
	private AliasService aliasService;
	@Inject
	private OtpCodeService codeService;

	protected CacheContainer cacheManager;

	@PostConstruct
	private void startup() {
		try {
			if (cacheManager == null) {
				synchronized (this) {
					if (cacheManager == null) {
						cacheManager = (CacheContainer) new InitialContext().lookup("java:jboss/infinispan/aerogear");
						if (EmbeddedCacheManager.class.isAssignableFrom(cacheManager.getClass())) {
							initContainerManaged(cacheManager);
						}
					}
				}

				logger.info("Using container managed Infinispan cache container, lookup={}",
						"java:jboss/infinispan/aerogear");
			}
		} catch (NamingException e) {
			logger.warn("Unable to lookup infinispan cache java:jboss/infinispan/aerogear");
		} finally {
			synchronized (this) {
				if (deviceToToken == null) {
					logger.warn(
							"Unable to locate infinispan cache installationverification, rolling back to ConcurrentHashMap impl!");
					deviceToToken = new ConcurrentHashMap<>();
				}
			}
		}
	}

	protected void initContainerManaged(CacheContainer cacheContainer) {
		try {
			EmbeddedCacheManager cacheManager = (EmbeddedCacheManager) cacheContainer;
			deviceToToken = cacheManager.getCache("otpCodes", true);

			Transport transport = cacheManager.getTransport();
			if (transport != null) {
				transport.getAddress().toString();
				cacheManager.getCacheManagerConfiguration().transport().siteId();
			}
		} catch (Exception e) {
			throw new RuntimeException("Failed to retrieve cache container", e);
		}
	}

	@Override
	public String retryDeviceVerification(String deviceToken, Variant variant) {
		Installation installation = installationDao.findInstallationForVariantByDeviceToken(variant.getVariantID(),
				deviceToken);

		return initiateDeviceVerification(installation, variant);
	}

	@Override
	public String initiateDeviceVerification(Installation installation, Variant variant) {
		// create a random string made up of numbers
		String verificationCode = RandomStringUtils.random(VERIFICATION_CODE_LENGTH, false, true);

		if (installation == null) {
			logger.warn("Missing installation, unable to send verification code!");
			return verificationCode;
		}

		Alias alias = aliasService.find(null, installation.getAlias());

		// Send verification messages only if variant name is not
		// DEVNULL_NOTIFICATIONS_VARIANT
		if (!DEVNULL_NOTIFICATIONS_VARIANT.equalsIgnoreCase(variant.getName())) {
			verificationService.sendVerificationMessage(alias == null ? null : alias.getPushApplicationId().toString(),
					installation.getAlias(), verificationCode);
		}

		OtpCodeKey okey = new OtpCodeKey(UUID.fromString(variant.getVariantID()), installation.getDeviceToken(),
				verificationCode);

		logger.debug("Add new otpCache code: {}, to variant: {}", okey.getCode(), okey.getVariantId());
		addToCache(okey);

		// Write code to cassandra with default ttl of one hour
		codeService.save(okey);

		return verificationCode;
	}

	@Override
	public VerificationResult verifyDevice(Installation installation, Variant variant,
			InstallationVerificationAttempt verificationAttempt) {
		OtpCodeKey okey = new OtpCodeKey(UUID.fromString(variant.getVariantID()), installation.getDeviceToken(),
				verificationAttempt.getCode());

		// Get code from local cache
		Set<Object> codes = deviceToToken.get(okey);

		// Reload from cassandra
		if (codes == null) {
			logger.debug("Missing code form local cache, trying to use cassandra backing cache");
			OtpCode code = codeService.findOne(okey);
			if (code != null) {
				logger.debug("Otp code fetched form cassandra backing cache");
				// Initialize local cache from backing cache
				addToCache(code.getKey());
				codes = deviceToToken.get(code.getKey());
			} else {
				logger.debug("Unable to locate verification code for tokenId: {}, VariantId: {}, code: {}",
						okey.getTokenId(), okey.getVariantId(), okey.getCode());
			}
		}

		if ((codes != null && codes.contains(verificationAttempt.getCode())) || isMasterCode(okey)) {
			installation.setEnabled(true);

			// Enable device
			installationDao.update(installation);

			// Remove from local cache
			deviceToToken.remove(okey);

			// Remove from cassandra
			codeService.delete(okey);

			// Enable OAuth2 User
			if (keycloakService.isInitialized() && verificationAttempt.isOauth2()) {
				keycloakService.createVerifiedUserIfAbsent(installation.getAlias(), verificationAttempt.getCode());
			}

			return VerificationResult.SUCCESS;
		} else {
			logger.debug("Verification attempt failed for tokenId: {}, VariantId: {}, code: {}", okey.getTokenId(),
					okey.getVariantId(), okey.getCode());
			return VerificationResult.FAIL;
		}
	}

	private boolean isMasterCode(OtpCodeKey okey) {
		// Support master code verification, should be used only for QA
		String masterCode = configuration.getMasterCode();

		return StringUtils.isNotEmpty(masterCode) && masterCode.equals(okey.getCode());
	}

	private void addToCache(OtpCodeKey okey) {
		Set<Object> codes;

		if (!deviceToToken.containsKey(okey)) {
			codes = new HashSet<Object>();
		} else {
			codes = deviceToToken.get(okey);
		}

		codes.add(okey.getCode());
		deviceToToken.putIfAbsent(okey, codes);
	}

	public void clearCache() {
		deviceToToken.clear();
	}
}