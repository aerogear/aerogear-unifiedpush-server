package org.jboss.aerogear.unifiedpush.service;

import org.jboss.aerogear.unifiedpush.api.Installation;

/**
 * Service used to manage installation verification cycle.
 */
public interface VerificationService {
	
	/**
	 * Sends a verification request to the device represented by the installation.
	 * @param installation device to verify
	 * @return the verification code issued to the installation.
	 */
	String initiateDeviceVerification(Installation installation);
	
	/**
	 * Attempts to verify the device (after a verification request has been issued prior to this point).
	 * @param installation installation trying to verify itself
	 * @param verificationCode verification code sent back by the device
	 * @return a {@link VerificationResult} signaling the outcome of the verification attempt.
	 */
	VerificationResult verifyDevice(Installation installation, String verificationCode);
	
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
