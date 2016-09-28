package org.jboss.aerogear.unifiedpush.api;

import com.fasterxml.jackson.annotation.JsonProperty;

public class InstallationVerificationAttempt {

	// Verification code
	private String code;
	// Device token
	private String deviceToken;
	// Indication to also activate oauth2 user
	private boolean oauth2 = false;

	public InstallationVerificationAttempt() {
	}

	public InstallationVerificationAttempt(String code, String deviceToken) {
		this.code = code;
		this.deviceToken = deviceToken;
		this.oauth2 = false;
	}

	public InstallationVerificationAttempt(String code, String deviceToken, boolean oauth2) {
		super();
		this.code = code;
		this.deviceToken = deviceToken;
		this.oauth2 = oauth2;
	}

	public String getCode() {
		return this.code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getDeviceToken() {
		return this.deviceToken;
	}

	public void setDeviceToken(String deviceToken) {
		this.deviceToken = deviceToken;
	}

	@JsonProperty(required = false)
	public boolean isOauth2() {
		return oauth2;
	}

	public void setOauth2(boolean oauth2) {
		this.oauth2 = oauth2;
	}
}
