package org.jboss.aerogear.unifiedpush.service.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.jboss.aerogear.unifiedpush.api.Alias;
import org.jboss.aerogear.unifiedpush.api.Installation;
import org.jboss.aerogear.unifiedpush.api.InstallationVerificationAttempt;
import org.jboss.aerogear.unifiedpush.api.PushApplication;
import org.jboss.aerogear.unifiedpush.api.Variant;
import org.jboss.aerogear.unifiedpush.cassandra.dao.NullUUID;
import org.jboss.aerogear.unifiedpush.cassandra.dao.model.OtpCode;
import org.jboss.aerogear.unifiedpush.cassandra.dao.model.OtpCodeKey;
import org.jboss.aerogear.unifiedpush.dao.InstallationDao;
import org.jboss.aerogear.unifiedpush.service.AliasService;
import org.jboss.aerogear.unifiedpush.service.PushApplicationService;
import org.jboss.aerogear.unifiedpush.service.VerificationPublisher.MessageType;
import org.jboss.aerogear.unifiedpush.service.VerificationService;
import org.jboss.aerogear.unifiedpush.service.impl.spring.IConfigurationService;
import org.jboss.aerogear.unifiedpush.service.impl.spring.IKeycloakService;
import org.jboss.aerogear.unifiedpush.service.impl.spring.IVerificationGatewayService;
import org.jboss.aerogear.unifiedpush.spring.ServiceCacheConfig;
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
	@Inject
	protected ServiceCacheConfig cacheService;
	@Inject
	private PushApplicationService pushApplicationService;

	@PostConstruct
	private void startup() {
		deviceToToken = cacheService.getOtpCache();
	}

	@Override
	public String retryDeviceVerification(String deviceToken, Variant variant) {
		Installation installation = installationDao.findInstallationForVariantByDeviceToken(variant.getVariantID(),
				deviceToken);

		return initiateDeviceVerification(installation, variant);
	}

	public String initiateDeviceVerification(String alias, MessageType type, Locale locale) {
		// create a random string made up of numbers
		String verificationCode = RandomStringUtils.random(VERIFICATION_CODE_LENGTH, false, true);

		Alias aliasObj = aliasService.find(null, alias);

		if (aliasObj == null) {
			logger.warn("Missing alias, unable to send verification code!");
			return null;
		}

		// Send Message
		verificationService.sendVerificationMessage(aliasObj.getPushApplicationId().toString(), alias, type,
				verificationCode, locale);

		OtpCodeKey okey = new OtpCodeKey(NullUUID.NULL.getUuid(), alias, verificationCode);

		logger.debug("Add new otpcache code: {}, to variant: {}", okey.getCode(), okey.getVariantId());
		addToCache(okey);

		// Write code to cassandra with default ttl of one hour
		codeService.save(okey);

		return verificationCode;
	}

	@Override
	@Deprecated
	/**
	 * Once only KC registration is supported, remove this method.
	 */
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
					installation.getAlias(), MessageType.REGISTER, verificationCode, Locale.ENGLISH);
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
	public VerificationResult verifyDevice(String alias, UUID variantId,
			InstallationVerificationAttempt verificationAttempt, boolean resetOnly) {
		Alias aliasObj = aliasService.find(null, alias);

		if (aliasObj == null) {
			logger.warn("Missing alias, unable to send verification code!");
			return null;
		}

		OtpCodeKey okey = new OtpCodeKey(variantId, alias, verificationAttempt.getCode());

		if (isValid(okey, verificationAttempt.getCode())) {
			// Remove from local cache
			deviceToToken.remove(okey);

			// Remove from cassandra
			codeService.delete(okey);

			// Enable OAuth2 User
			PushApplication pushApplication = pushApplicationService.findByVariantID(variantId.toString());
			String realmName = keycloakService.getRealmName(pushApplication.getName());
			if (verificationAttempt.isOauth2()) {
				if (resetOnly) {
					keycloakService.resetUserPassword(alias, verificationAttempt.getCode(), realmName);
				} else {
					Collection<UserTenantInfo> tenantRelations = aliasService.getTenantRelations(alias);
					keycloakService.createVerifiedUserIfAbsent(alias, verificationAttempt.getCode(), tenantRelations, realmName);
				}
			}

			return VerificationResult.SUCCESS;
		} else {
			logger.debug("Verification attempt failed for tokenId: {}, VariantId: {}, code: {}", okey.getTokenId(),
					okey.getVariantId(), okey.getCode());
			return VerificationResult.FAIL;
		}
	}

	@Override
	@Deprecated
	/**
	 * Once only KC registration is supported, remove this method.
	 */
	public VerificationResult verifyDevice(Installation installation, Variant variant,
			InstallationVerificationAttempt verificationAttempt) {
		OtpCodeKey okey = new OtpCodeKey(UUID.fromString(variant.getVariantID()), installation.getDeviceToken(),
				verificationAttempt.getCode());

		if (isValid(okey, verificationAttempt.getCode())) {
			installation.setEnabled(true);

			// Enable device
			installationDao.update(installation);

			// Remove from local cache
			deviceToToken.remove(okey);

			// Remove from cassandra
			codeService.delete(okey);

			// Enable OAuth2 User
			PushApplication pushApplication = pushApplicationService.findByVariantID(variant.getVariantID());
			String realmName = keycloakService.getRealmName(pushApplication.getName());
			if (verificationAttempt.isOauth2()) {
				keycloakService.createVerifiedUserIfAbsent(installation.getAlias(), verificationAttempt.getCode(), null, realmName);
			}

			return VerificationResult.SUCCESS;
		} else {
			logger.debug("Verification attempt failed for tokenId: {}, VariantId: {}, code: {}", okey.getTokenId(),
					okey.getVariantId(), okey.getCode());
			return VerificationResult.FAIL;
		}
	}

	private boolean isValid(OtpCodeKey okey, String code) {
		Set<Object> codes = getCodes(okey);

		if (codes != null) {
			if (codes.contains(code) || isMasterCode(okey)) {
				return true;
			} else {
				loadBehind(okey);
				codes = deviceToToken.get(okey);
				if (codes.contains(code)) {
					return true;
				}
			}
		}

		return false;
	}

	private Set<Object> getCodes(OtpCodeKey okey) {
		// Get code from local cache
		Set<Object> codes = deviceToToken.get(okey);

		// Reload from cassandra
		if (codes == null) {
			loadBehind(okey);
			codes = deviceToToken.get(okey);
		}

		return codes;
	}

	private void loadBehind(OtpCodeKey okey) {
		logger.debug("Missing code form local cache, trying to use cassandra backing cache");

		OtpCode code = codeService.findOne(okey);
		if (code != null) {
			logger.debug("Otp code fetched form cassandra backing cache");
			// Initialize local cache from backing cache
			addToCache(code.getKey());
		} else {
			logger.debug("Unable to locate verification code for tokenId: {}, VariantId: {}, code: {}",
					okey.getTokenId(), okey.getVariantId(), okey.getCode());
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