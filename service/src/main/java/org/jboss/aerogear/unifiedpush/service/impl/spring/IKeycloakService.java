package org.jboss.aerogear.unifiedpush.service.impl.spring;

import java.util.Collection;

import org.jboss.aerogear.unifiedpush.api.PushApplication;
import org.jboss.aerogear.unifiedpush.service.impl.UserTenantInfo;

public interface IKeycloakService {
	public static final String CACHE_NAME = "variant-ids-per-clientid";

	void createClientIfAbsent(PushApplication pushApplication);

	void removeClient(PushApplication pushApplication);

	void createVerifiedUserIfAbsent(String userName, String password, Collection<UserTenantInfo> userTenantInfos, String realm);

	boolean exists(String userNam, String applicationName);

	void delete(String userName, String applicationName);

	void updateUserPassword(String aliasId, String currentPassword, String newPassword, String applicationName);

	void resetUserPassword(String aliasId, String newPassword, String applicationName);

	boolean isInitialized(String realmName);

	String strip(String fqdn);

	String seperator();

	void updateTenantsExistingUser(String representativeAlias, Collection<UserTenantInfo> tenantRelations, String applicationName);

	String getRealmName(String applicationName);
}
