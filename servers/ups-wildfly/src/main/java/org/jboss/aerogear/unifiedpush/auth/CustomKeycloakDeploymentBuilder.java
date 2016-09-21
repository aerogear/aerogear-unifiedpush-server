package org.jboss.aerogear.unifiedpush.auth;

import java.io.InputStream;

import org.keycloak.adapters.KeycloakDeploymentBuilder;
import org.keycloak.representations.adapters.config.AdapterConfig;

public class CustomKeycloakDeploymentBuilder extends KeycloakDeploymentBuilder {

	public CustomKeycloakDeploymentBuilder() {
		deployment = new CustomKeycloakDeployment();
	}

	public static CustomKeycloakDeployment build(InputStream is) {
		AdapterConfig adapterConfig = loadAdapterConfig(is);

		return (CustomKeycloakDeployment) new CustomKeycloakDeploymentBuilder().internalBuild(adapterConfig);
	}

}
