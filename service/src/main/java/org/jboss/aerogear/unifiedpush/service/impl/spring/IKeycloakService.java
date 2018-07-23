package org.jboss.aerogear.unifiedpush.service.impl.spring;

import java.util.List;

import org.jboss.aerogear.unifiedpush.api.PushApplication;
import org.keycloak.authentication.actiontoken.verifyemail.VerifyEmailActionToken;
import org.keycloak.common.VerificationException;
import org.springframework.cache.annotation.Cacheable;

public interface IKeycloakService {
	public static final String CACHE_NAME = "variant-ids-per-clientid";

	void createClientIfAbsent(PushApplication pushApplication);

	void removeClient(PushApplication pushApplicaiton);

	void createVerifiedUserIfAbsent(String userName, String password);

	boolean exists(String userName);

	void delete(String userName);

	@Cacheable(value = IKeycloakService.CACHE_NAME, unless = "#result == null")
	List<String> getVariantIdsFromClient(String clientId);

	void updateUserPassword(String aliasId, String currentPassword, String newPassword);

	boolean isInitialized();

	String strip(String fqdn);
	
	VerifyEmailActionToken validateEmailActionToken(String token) throws VerificationException;
}
