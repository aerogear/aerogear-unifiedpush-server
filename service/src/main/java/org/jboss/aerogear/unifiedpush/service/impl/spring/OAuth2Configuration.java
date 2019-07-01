package org.jboss.aerogear.unifiedpush.service.impl.spring;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.jboss.aerogear.unifiedpush.system.ConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Serve system properties either from Configuration or from System.getProperty.
 */
@Service
public class OAuth2Configuration implements IOAuth2Configuration {
	private static final String KEY_OAUTH2_ENABLE = "aerogear.config.oauth2.enable";

	private static final String KEY_OAUTH2_SERVER_URL = "aerogear.config.oauth2.keycloak.url";
	private static final String KEY_OAUTH2_UPS_REALM = "aerogear.config.oauth2.ups.realm.name";
	private static final String KEY_OAUTH2_UPSI_REALM = "aerogear.config.oauth2.upsi.realm.name";
	private static final String KEY_OAUTH2_UPS_MASTER_REALM = "aerogear.config.oauth2.ups.master.realm.name";

	private static final String KEY_OAUTH2_ADMIN_USERNAME = "aerogear.config.oauth2.admin.username";
	private static final String KEY_OAUTH2_ADMIN_PASSWORD = "aerogear.config.oauth2.admin.password";

	private static final String KEY_OAUTH2_ADMIN_CLIENT_ID = "aerogear.config.oauth2.ups.admin.client";
	public static final String KEY_OAUTH2_ENFORE_DOMAIN = "aerogear.config.oauth2.enforce.rooturl.domain";
	private static final String KEY_OAUTH2_ENFORCE_PROTOCOL = "aerogear.config.oauth2.enforce.rooturl.protocol";
	private static final String KEY_OAUTH2_ENFORE_SEPERATOR = "aerogear.config.oauth2.enforce.rooturl.seperator";

	private static final String DEFAULT_OAUTH2_SERVER_URL = "/auth";

	public static final String DEFAULT_OAUTH2_UPS_REALM = "unifiedpush";
	public static final String DEFAULT_OAUTH2_UPSI_REALM = "unifiedpush-installations";
	public static final String DEFAULT_OAUTH2_UPS_MASTER_REALM = "master";
	public static final String DEFAULT_SUBDOMAIN_SEPERATOR = "-";

	private static ConfigurationEnvironment configuration;

	public boolean isOAuth2Enabled() {
		return getProperty(KEY_OAUTH2_ENABLE, false);
	}

	public String getOAuth2Url() {
		return getProperty(KEY_OAUTH2_SERVER_URL, DEFAULT_OAUTH2_SERVER_URL);
	}

	public String getUpsRealm() {
		return getProperty(KEY_OAUTH2_UPS_REALM, DEFAULT_OAUTH2_UPS_REALM);
	}

	public String getUpsiRealm() {
		return getProperty(KEY_OAUTH2_UPSI_REALM, DEFAULT_OAUTH2_UPSI_REALM);
	}
	public String getUpsMasterRealm() {
		return getProperty(KEY_OAUTH2_UPS_MASTER_REALM, DEFAULT_OAUTH2_UPS_MASTER_REALM);
	}

	public String getAdminClient() {
		return getProperty(KEY_OAUTH2_ADMIN_CLIENT_ID, StringUtils.EMPTY);
	}

	public String getAdminUserName() {
		return getProperty(KEY_OAUTH2_ADMIN_USERNAME, StringUtils.EMPTY);
	}

	public String getAdminPassword() {
		return getProperty(KEY_OAUTH2_ADMIN_PASSWORD, StringUtils.EMPTY);
	}

	public String getRooturlDomain() {
		return getProperty(KEY_OAUTH2_ENFORE_DOMAIN, StringUtils.EMPTY);
	}

	public String getRooturlProtocol() {
		return getProperty(KEY_OAUTH2_ENFORCE_PROTOCOL, StringUtils.EMPTY);
	}

	public DomainMatcher getRooturlMatcher() {
		return DomainMatcher.fromString(getProperty(KEY_OAUTH2_ENFORE_SEPERATOR, DEFAULT_SUBDOMAIN_SEPERATOR));
	}

	private Boolean getProperty(String key, Boolean defaultValue) {
		if (configuration == null) {
			return getSystemProperty(key, defaultValue);
		}

		return configuration.getProperty(key, defaultValue);
	}

	private static String getProperty(String key, String defaultValue) {
		if (configuration == null) {
			return getSystemProperty(key, defaultValue);
		}

		return configuration.getProperty(key, defaultValue);
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

	public static String getStaticOAuth2Url() {
		return getProperty(KEY_OAUTH2_SERVER_URL, DEFAULT_OAUTH2_SERVER_URL);
	}

	public static String getStaticUpsRealm() {
		return getProperty(KEY_OAUTH2_UPS_REALM, DEFAULT_OAUTH2_UPS_REALM);
	}

	public static String getStaticUpsiRealm() {
		return getProperty(KEY_OAUTH2_UPSI_REALM, DEFAULT_OAUTH2_UPSI_REALM);
	}

	public ConfigurationEnvironment getConfiguration() {
		return configuration;
	}

	@Autowired
	public void setConfiguration(ConfigurationEnvironment configuration) {
		OAuth2Configuration.configuration = configuration;
	}

	public enum DomainMatcher {
		// Valid subdomain, match as few characters as possible (First
		// occurrence).
		DOT(".", "(.*?)[.].*") {
			@Override
			public String rootUrl(String protocol, String domain, String application) {
				return protocol + "://" + application + seperator() + domain;
			}
		},
		// Logical subdomain, match as many characters as possible (Last
		// occurrence).
		DASH("-", "(.*)[-].*") {
			@Override
			public String rootUrl(String protocol, String domain, String application) {
				return protocol + "://" + application + seperator() + domain;
			}
		},

		NONE("*", "(.*)") {
			@Override
			public String rootUrl(String protocol, String domain, String application) {
				return protocol + "://" + application;
			}
		};

		private final Pattern pattern;
		private final String seperator;

		private DomainMatcher(String seperator, String pattern) {
			this.seperator = seperator;
			this.pattern = Pattern.compile(pattern);
		}

		public String matches(String toMatch) {
			Matcher matcher = pattern.matcher(toMatch);

			if (matcher.matches()) {
				return matcher.group(1);
			}

			return null;
		}

		public String seperator() {
			return seperator;
		}

		public static DomainMatcher fromString(String seperator) {
			if (DOT.seperator().equals(seperator)) {
				return DomainMatcher.DOT;
			} else if (DASH.seperator().equals(seperator)) {
				return DomainMatcher.DASH;
			}

			return DomainMatcher.NONE;
		}

		public abstract String rootUrl(String protocol, String domain, String application);
	}

}
