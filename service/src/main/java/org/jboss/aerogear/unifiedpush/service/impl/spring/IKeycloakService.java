package org.jboss.aerogear.unifiedpush.service.impl.spring;

import java.util.Collection;
import java.util.List;

import org.jboss.aerogear.unifiedpush.api.PushApplication;
import org.jboss.aerogear.unifiedpush.service.impl.UserTenantInfo;

public interface IKeycloakService {
	String CACHE_NAME = "variant-ids-per-clientid";
	String KEYCLOAK_ROLE_USER = "installation";

	void createClientIfAbsent(PushApplication pushApplication);

	void removeClient(PushApplication pushApplication);

	void createVerifiedUserIfAbsent(String userName, String password, Collection<UserTenantInfo> userTenantInfos, String realm);

	boolean exists(String userName, String applicationName);

	void delete(String userName, String applicationName);

	void updateUserPassword(String aliasId, String currentPassword, String newPassword, String applicationName);

	void resetUserPassword(String aliasId, String newPassword, String applicationName);

	String strip(String fqdn);

	String separator();

	void updateTenantsExistingUser(String representativeAlias, Collection<UserTenantInfo> tenantRelations, String applicationName);

	String getRealmName(String applicationName);

	void createRealmIfAbsent(String realmName);

	String getUserAccessToken(String userName, String password, String realm, String appId);

	void setPasswordUpdateRequired(String userName, String realm, boolean isRequired);

	void setDirectAccessGrantsEnabled(String appName, String realmName, boolean directAccessGrantsEnabled);

	List<String> getUtr(String userName, String realm);

	void addUserRealmRoles(List<String> roles, String userName, String realm);

	void disableUserCredentials(String userName, String realm);
}
