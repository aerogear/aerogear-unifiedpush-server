package org.jboss.aerogear.unifiedpush.rest.config;

public interface IKCConfig {
	public static final String KEY_AUTHENTICATION_SERVER_URL = "ups.auth.server.url";
	public static final String KEY_AUTHENTICATION_UPS_REALM = "ups.realm.name";
	public static final String KEY_AUTHENTICATION_UPSI_REALM = "upsi.realm.name";

	public static final String DEFAULT_AUTHENTICATION_SERVER_URL = "/auth";
	public static final String DEFAULT_AUTHENTICATION_UPS_REALM = "unifiedpush";
	public static final String DEFAULT_AUTHENTICATION_UPSI_REALM = "unifiedpush-installations";
}
