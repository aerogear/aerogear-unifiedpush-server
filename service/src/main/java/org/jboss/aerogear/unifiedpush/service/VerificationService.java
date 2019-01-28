package org.jboss.aerogear.unifiedpush.service;

import java.util.Locale;
import java.util.UUID;

import org.jboss.aerogear.unifiedpush.api.Installation;
import org.jboss.aerogear.unifiedpush.api.InstallationVerificationAttempt;
import org.jboss.aerogear.unifiedpush.api.Variant;
import org.jboss.aerogear.unifiedpush.service.VerificationPublisher.MessageType;

/**
 * Service used to manage installation verification cycle.
 */
public interface VerificationService {
	String DEVNULL_NOTIFICATIONS_VARIANT = "NULL-NOTIFICATIONS-VARIANT";

	static boolean isDevNullVariant(String variantName) {
		return VerificationService.DEVNULL_NOTIFICATIONS_VARIANT.equals(variantName);
	}

	/**
	 * Sends a verification request to the device represented by the installation.
	 *
	 * @param installation device to verify
	 * @param variant      the installation's variant
	 * @return the verification code issued to the installation.
	 */
	String initiateDeviceVerification(Installation installation, Variant variant);

	String initiateDeviceVerification(String alias, MessageType type, Locale locale);

	String retryDeviceVerification(String deviceToken, Variant variant);

	/**
	 * Attempts to verify the device (after a verification request has been issued
	 * prior to this point).
	 *
	 * @param installation        The device installation.
	 * @param variant             device Variant.
	 * @param verificationAttempt verification params sent back by the device
	 * @return a {@link VerificationResult} signaling the outcome of the
	 *         verification attempt.
	 */
	VerificationResult verifyDevice(Installation installation, Variant variant,
			InstallationVerificationAttempt verificationAttempt);

	VerificationResult verifyDevice(String alias, UUID variantId, InstallationVerificationAttempt verificationAttempt,
			boolean resetOnly);

	/**
	 * Clear runtime cache
	 */
	void clearCache();

	enum VerificationResult {
		/**
		 * Verification succeeded
		 */
		SUCCESS,
		/**
		 * Verification was wrong
		 */
		FAIL
	}
}
