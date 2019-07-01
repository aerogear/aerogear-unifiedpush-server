package org.jboss.aerogear.unifiedpush.service.impl.spring;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.jboss.aerogear.unifiedpush.api.PushApplication;
import org.jboss.aerogear.unifiedpush.service.impl.UserTenantInfo;
import org.springframework.cache.annotation.Cacheable;

public interface IKeycloakService {
	public static final String CACHE_NAME = "variant-ids-per-clientid";

	void createClientIfAbsent(PushApplication pushApplication);

	void removeClient(PushApplication pushApplication);

	void createVerifiedUserIfAbsent(String userName, String password, Collection<UserTenantInfo> userTenantInfos, String realm);

	boolean exists(String userNam, String applicationName);

	void delete(String userName, String applicationName);

	@Cacheable(value = IKeycloakService.CACHE_NAME, unless = "#result == null")
	List<String> getVariantIdsFromClient(String clientId, String realm);

	void updateUserPassword(String aliasId, String currentPassword, String newPassword, String applicationName);

	void resetUserPassword(String aliasId, String newPassword, String applicationName);

	boolean isInitialized(String realmName);

	String strip(String fqdn);

	String seperator();

	void updateTenantsExistingUser(String representativeAlias, Collection<UserTenantInfo> tenantRelations, String applicationName);

	String getRealmName(String applicationName);
}
