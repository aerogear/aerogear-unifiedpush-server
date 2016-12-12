package org.jboss.aerogear.unifiedpush.auth;

import java.io.InputStream;

import org.apache.commons.lang3.StringUtils;
import org.jboss.aerogear.unifiedpush.service.OAuth2ConfigurationBuilder;
import org.keycloak.adapters.KeycloakDeploymentBuilder;
import org.keycloak.representations.adapters.config.AdapterConfig;

public class CustomKeycloakDeploymentBuilder extends KeycloakDeploymentBuilder {

	public CustomKeycloakDeploymentBuilder() {
		deployment = new CustomKeycloakDeployment();
	}

	public static CustomKeycloakDeployment build(InputStream is) {
		AdapterConfig adapterConfig = loadAdapterConfig(is);

		// Override realm attributes from system properties if exists.
		String upsRealmName = OAuth2ConfigurationBuilder.getUpsRealm();
		String upsiRealmName = OAuth2ConfigurationBuilder.getUpsiRealm();
		String upsAuthServer = OAuth2ConfigurationBuilder.getOAuth2Url();

		// This can be useful when we need to override properties from static WEB-INF json files.
		if (OAuth2ConfigurationBuilder.DEFAULT_OAUTH2_UPS_REALM.equals(adapterConfig.getRealm())) {
			adapterConfig.setRealm(upsRealmName);
		}

		if (OAuth2ConfigurationBuilder.DEFAULT_OAUTH2_UPSI_REALM.equals(adapterConfig.getRealm())
				&& StringUtils.isNotEmpty(upsiRealmName)) {
			adapterConfig.setRealm(upsiRealmName);
		}

		adapterConfig.setAuthServerUrl(upsAuthServer);

		return (CustomKeycloakDeployment) new CustomKeycloakDeploymentBuilder().internalBuild(adapterConfig);
	}

}
