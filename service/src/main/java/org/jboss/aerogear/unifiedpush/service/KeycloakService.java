package org.jboss.aerogear.unifiedpush.service;

import java.util.List;

import org.jboss.aerogear.unifiedpush.api.PushApplication;

public interface KeycloakService {
	void createClientIfAbsent(PushApplication pushApplication);

	void createUserIfAbsent(String alias);

	void createVerifiedUserIfAbsent(String userName, String password);

	boolean exists(String userName);

	void delete(String userName);

	List<String> getVariantIdsFromClient(String clientID);

	void updateUserPassword(String aliasId, String currentPassword, String newPassword);

	boolean isInitialized();
}
