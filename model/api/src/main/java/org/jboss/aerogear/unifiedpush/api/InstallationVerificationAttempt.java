package org.jboss.aerogear.unifiedpush.api;

import com.fasterxml.jackson.annotation.JsonProperty;

public class InstallationVerificationAttempt {
	
	private String code;
	private String deviceToken;
	private boolean enableOauth;
	
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
	public boolean getEnableOauth() {
		return this.enableOauth;
	}

	public void setEnableOauth(boolean enableOauth) {
		this.enableOauth = enableOauth;
	}
}
