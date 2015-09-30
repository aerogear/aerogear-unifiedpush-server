package org.jboss.aerogear.unifiedpush.api;

public class InstallationVerificationAttempt {
	
	private String code;
	private String deviceToken;
	
	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getDeviceToken() {
		return deviceToken;
	}

	public void setDeviceToken(String deviceToken) {
		this.deviceToken = deviceToken;
	}

}
