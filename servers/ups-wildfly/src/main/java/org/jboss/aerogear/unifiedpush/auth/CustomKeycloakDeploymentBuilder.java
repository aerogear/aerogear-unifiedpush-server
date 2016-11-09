package org.jboss.aerogear.unifiedpush.auth;

import java.io.InputStream;

import org.apache.commons.lang.StringUtils;
import org.jboss.aerogear.unifiedpush.rest.config.IKCConfig;
import org.keycloak.adapters.KeycloakDeploymentBuilder;
import org.keycloak.representations.adapters.config.AdapterConfig;

public class CustomKeycloakDeploymentBuilder extends KeycloakDeploymentBuilder {

	public CustomKeycloakDeploymentBuilder() {
		deployment = new CustomKeycloakDeployment();
	}

	public static CustomKeycloakDeployment build(InputStream is) {
		AdapterConfig adapterConfig = loadAdapterConfig(is);

		// Override realm attributes from system properties if exists.
		String upsRealmName = System.getProperty(IKCConfig.KEY_AUTHENTICATION_UPS_REALM);
		String upsiRealmName = System.getProperty(IKCConfig.KEY_AUTHENTICATION_UPSI_REALM);
		String upsAuthServer = System.getProperty(IKCConfig.KEY_AUTHENTICATION_SERVER_URL);

		if (IKCConfig.DEFAULT_AUTHENTICATION_UPS_REALM.equals(adapterConfig.getRealm())
				&& StringUtils.isNotEmpty(upsRealmName)) {
			adapterConfig.setRealm(upsRealmName);
		}

		if (IKCConfig.DEFAULT_AUTHENTICATION_UPSI_REALM.equals(adapterConfig.getRealm())
				&& StringUtils.isNotEmpty(upsiRealmName)) {
			adapterConfig.setRealm(upsiRealmName);
		}

		if (IKCConfig.DEFAULT_AUTHENTICATION_SERVER_URL.equals(adapterConfig.getAuthServerUrl())
				&& StringUtils.isNotEmpty(upsAuthServer)) {
			adapterConfig.setAuthServerUrl(upsAuthServer);
		}

		return (CustomKeycloakDeployment) new CustomKeycloakDeploymentBuilder().internalBuild(adapterConfig);
	}

}
