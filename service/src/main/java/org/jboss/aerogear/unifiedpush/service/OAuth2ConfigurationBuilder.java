package org.jboss.aerogear.unifiedpush.service;

import org.apache.commons.lang3.StringUtils;
import org.jboss.aerogear.unifiedpush.system.ConfigurationEnvironment;

/**
 * Serve system properties either from Configuration or from System.getProperty.
 */
public class OAuth2ConfigurationBuilder {
	private static final String KEY_OAUTH2_ENABLE = "aerogear.config.oauth2.enable";

	private static final String KEY_OAUTH2_SERVER_URL = "aerogear.config.oauth2.keycloak.url";
	private static final String KEY_OAUTH2_UPS_REALM = "aerogear.config.oauth2.ups.realm.name";
	private static final String KEY_OAUTH2_UPSI_REALM = "aerogear.config.oauth2.upsi.realm.name";

	private static final String KEY_OAUTH2_ADMIN_USERNAME = "aerogear.config.oauth2.admin.username";
	private static final String KEY_OAUTH2_ADMIN_PASSWORD = "aerogear.config.oauth2.admin.password";

	private static final String KEY_OAUTH2_ADMIN_CLIENT_ID = "aerogear.config.oauth2.ups.admin.client";
	private static final String KEY_OAUTH2_ENFORE_DOMAIN = "aerogear.config.oauth2.enforce.rooturl.domain";
	private static final String KEY_OAUTH2_ENFORCE_PROTOCOL = "aerogear.config.oauth2.enforce.rooturl.protocol";

	private static final String DEFAULT_OAUTH2_SERVER_URL = "/auth";

	public static final String DEFAULT_OAUTH2_UPS_REALM = "unifiedpush";
	public static final String DEFAULT_OAUTH2_UPSI_REALM = "unifiedpush-installations";

	private static final OAuth2ConfigurationBuilder OAUTH2 = new OAuth2ConfigurationBuilder();
	private static ConfigurationEnvironment conf;

	public static OAuth2ConfigurationBuilder get(){
		return OAUTH2;
	}

	public static OAuth2ConfigurationBuilder build(ConfigurationEnvironment conf) {
		OAuth2ConfigurationBuilder.conf = conf;
		return OAUTH2;
	}

	public static boolean isOAuth2Enabled() {
		return getProperty(KEY_OAUTH2_ENABLE, false);
	}

	public static String getOAuth2Url() {
		return getProperty(KEY_OAUTH2_SERVER_URL, DEFAULT_OAUTH2_SERVER_URL);
	}

	public static String getUpsRealm() {
		return getProperty(KEY_OAUTH2_UPS_REALM, DEFAULT_OAUTH2_UPS_REALM);
	}

	public static String getUpsiRealm() {
		return getProperty(KEY_OAUTH2_UPSI_REALM, DEFAULT_OAUTH2_UPSI_REALM);
	}

	public static String getAdminClient() {
		return getProperty(KEY_OAUTH2_ADMIN_CLIENT_ID, StringUtils.EMPTY);
	}

	public static String getAdminUserName() {
		return getProperty(KEY_OAUTH2_ADMIN_USERNAME, StringUtils.EMPTY);
	}

	public static String getAdminPassword() {
		return getProperty(KEY_OAUTH2_ADMIN_PASSWORD, StringUtils.EMPTY);
	}

	public static String getRooturlDomain() {
		return getProperty(KEY_OAUTH2_ENFORE_DOMAIN, StringUtils.EMPTY);
	}

	public static String getRooturlProtocol() {
		return getProperty(KEY_OAUTH2_ENFORCE_PROTOCOL, StringUtils.EMPTY);
	}

	private static Boolean getProperty (String key, Boolean defaultValue){
		if (conf == null){
			return getSystemProperty(key, defaultValue);
		}

		return conf.getProperty(key, defaultValue);
	}

	private static String getProperty (String key, String defaultValue){
		if (conf == null){
			return getSystemProperty(key, defaultValue);
		}

		return conf.getProperty(key, defaultValue);
	}


	private static String getSystemProperty(String key, String defaultValue) {
		String value = System.getProperty(key);
		if (value == null) {
			return defaultValue;
		}
		return value;
	}

	private static Boolean getSystemProperty(String key, Boolean defaultValue) {
		String value = System.getProperty(key);
		if (value == null) {
			return defaultValue;
		}
		return Boolean.valueOf(value);
	}
}
