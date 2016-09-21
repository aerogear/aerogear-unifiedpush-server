package org.jboss.aerogear.unifiedpush.auth;

import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.common.util.KeycloakUriBuilder;

public class CustomKeycloakDeployment extends KeycloakDeployment {

	public void setAuthUrl(KeycloakUriBuilder authUrl) {
		this.authUrl = authUrl;
	}

	public void setRealmInfoUrl(String realmInfoUrl) {
		this.realmInfoUrl = realmInfoUrl;
	}

	public void setTokenUrl(String tokenUrl) {
		this.tokenUrl = tokenUrl;
	}

	public void setLogoutUrl(KeycloakUriBuilder logoutUrl) {
		this.logoutUrl = logoutUrl;
	}

	public void setAccountUrl(String accountUrl) {
		this.accountUrl = accountUrl;
	}

	public void setRegisterNodeUrl(String registerNodeUrl) {
		this.registerNodeUrl = registerNodeUrl;
	}

	public void setUnregisterNodeUrl(String unregisterNodeUrl) {
		this.unregisterNodeUrl = unregisterNodeUrl;
	}

}
