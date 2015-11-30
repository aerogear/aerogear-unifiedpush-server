package org.jboss.aerogear.unifiedpush.service;

import org.jboss.aerogear.unifiedpush.api.Installation;
import org.jboss.aerogear.unifiedpush.api.Variant;

/**
 * Service used to manage installation verification cycle.
 */
public interface VerificationService {
	
	/**
	 * Sends a verification request to the device represented by the installation.
	 * @param installation device to verify
	 * @param installation the installation's variant
	 * @return the verification code issued to the installation.
	 */
	String initiateDeviceVerification(Installation installation, Variant variant);
	
	String retryDeviceVerification(String deviceToken, Variant variant);
	
	/**
	 * Attempts to verify the device (after a verification request has been issued prior to this point).
	 * @param variantID variant ID of the installation
	 * @param deviceToken device token of the installation trying to verify itself
	 * @param verificationCode verification code sent back by the device
	 * @return a {@link VerificationResult} signaling the outcome of the verification attempt.
	 */
	VerificationResult verifyDevice(Installation installation, Variant variant, String verificationCode);
	
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
		 * The verification attempt for the installation is unknown by the system.
		 * Whether this installation was never issued a verification request before or the verification
		 * data has expired is unspecified.
		 */
		UNKNOWN
	}
}
