package org.jboss.aerogear.unifiedpush.service.wrap;

import java.util.List;

import javax.ejb.Stateless;
import javax.interceptor.Interceptors;

import org.jboss.aerogear.unifiedpush.api.PushApplication;
import org.jboss.aerogear.unifiedpush.service.KeycloakService;
import org.jboss.aerogear.unifiedpush.service.impl.spring.IKeycloakService;
import org.jboss.aerogear.unifiedpush.spring.SpringContextInterceptor;
import org.springframework.beans.factory.annotation.Autowired;

@Stateless
@Wrapper
@Interceptors(SpringContextInterceptor.class)
public class KeycloakServiceWrapper implements KeycloakService {

	@Autowired
	private IKeycloakService keycloakService;

	@Override
	public void createClientIfAbsent(PushApplication pushApplication) {
		keycloakService.createClientIfAbsent(pushApplication);
	}

	@Override
	public void createUserIfAbsent(String alias) {
		keycloakService.createUserIfAbsent(alias);
	}

	@Override
	public void createVerifiedUserIfAbsent(String userName, String password) {
		keycloakService.createVerifiedUserIfAbsent(userName, password);
	}

	@Override
	public boolean exists(String userName) {
		return keycloakService.exists(userName);
	}

	@Override
	public void delete(String userName) {
		keycloakService.delete(userName);
	}

	@Override
	public List<String> getVariantIdsFromClient(String clientId) {
		return keycloakService.getVariantIdsFromClient(clientId);
	}

	@Override
	public void updateUserPassword(String aliasId, String currentPassword, String newPassword) {
		keycloakService.updateUserPassword(aliasId, currentPassword, newPassword);

	}

	@Override
	public boolean isInitialized() {
		return keycloakService.isInitialized();
	}
}
