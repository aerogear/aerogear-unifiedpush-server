package org.jboss.aerogear.unifiedpush.service;

import java.util.List;

import org.jboss.aerogear.unifiedpush.api.PushApplication;

public interface KeycloakService {
	void createClientIfAbsent(PushApplication pushApplication);

	void updateUser(String userName, String password);

	void delete(String userName);

	void createUserIfAbsent(String alias);

	List<String> getVariantIdsFromClient(String clientID);

	void updateUserPassword(String aliasId, String currentPassword, String newPassword);

	boolean isInitialized();
}
