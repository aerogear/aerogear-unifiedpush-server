package org.jboss.aerogear.unifiedpush.service;

import org.jboss.aerogear.unifiedpush.api.Installation;
import org.jboss.aerogear.unifiedpush.api.InstallationVerificationAttempt;
import org.jboss.aerogear.unifiedpush.api.Variant;

/**
 * Service used to manage installation verification cycle.
 */
public interface VerificationService {
	public static final String DEVNULL_NOTIFICATIONS_VARIANT = "NULL-NOTIFICATIONS-VARIANT";

	/**
	 * Sends a verification request to the device represented by the
	 * installation.
	 *
	 * @param installation
	 *            device to verify
	 * @param variant
	 *            the installation's variant
	 * @return the verification code issued to the installation.
	 */
	String initiateDeviceVerification(Installation installation, Variant variant);

	String retryDeviceVerification(String deviceToken, Variant variant);

	/**
	 * Attempts to verify the device (after a verification request has been
	 * issued prior to this point).
	 *
	 * @param installation
	 *            The device installation.
	 * @param variant
	 *            device Variant.
	 * @param verificationAttempt
	 *            verification params sent back by the device
	 * @return a {@link VerificationResult} signaling the outcome of the
	 *         verification attempt.
	 */
	VerificationResult verifyDevice(Installation installation, Variant variant,
			InstallationVerificationAttempt verificationAttempt);

	/**
	 * Clear runtime cache
	 */
	void clearCache();

	public enum VerificationResult {
		/**
		 * Verification succeeded
		 */
		SUCCESS,
		/**
		 * Verification was wrong
		 */
		FAIL,
		/**
		 * The verification attempt for the installation is unknown by the
		 * system. Whether this installation was never issued a verification
		 * request before or the verification data has expired is unspecified.
		 */
		UNKNOWN
	}
}
