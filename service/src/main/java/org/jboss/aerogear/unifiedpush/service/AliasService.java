/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.unifiedpush.service;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.jboss.aerogear.unifiedpush.api.Alias;
import org.jboss.aerogear.unifiedpush.api.PushApplication;
import org.jboss.aerogear.unifiedpush.cassandra.dao.model.UserKey;
import org.jboss.aerogear.unifiedpush.service.impl.AliasServiceImpl.Associated;
import org.jboss.aerogear.unifiedpush.service.impl.UserTenantInfo;

public interface AliasService {
	/**
	 * Creates <b>distinct</b> (according to
	 * {@code {@link org.jboss.aerogear.unifiedpush.api.Alias#getEmail()}})
	 * aliases, mirroring the data received in {@code aliases}, and associates
	 * them to the given application. Note that this application's existing
	 * aliases will be overwritten by the newly created aliases.

	 * @param pushApplication
	 *            push application to associate the aliases to.
	 * @param aliases
	 *            aliases list to match against.
	 * @param oauth2
	 *            synchronize state to identity provider
	 * @return new {@link org.jboss.aerogear.unifiedpush.api.Alias} list
	 */
	List<Alias> addAll(PushApplication pushApplication, List<Alias> aliases, boolean oauth2);

	/**
	 * updates specific user password
	 *
	 * @param aliasId
	 *            - current logged in user id
	 * @param currentPassword
	 *            - user password
	 * @param newPassword
	 *            - user old password
	 */
	void updateAliasePassword(String aliasId, String currentPassword, String newPassword);

	boolean registered(String alias);

	Associated associated(String fqdn, String alias);

	Alias find(String pushApplicationId, String alias);

	Alias find(UUID pushApplicationId, UUID userId);

	/**
	 * Removes all entries matching provided alias
	 *
	 * @param pushApplicationId
	 * 		related push application id
	 * @param alias
	 * 		required alias
	 * @return list of actually removed user keys
	 */
	List<UserKey> remove(UUID pushApplicationId, String alias);

	/**
	 * Remove alias and user in KC.
	 *
	 * @param pushApplicationId
	 *            related push application id
	 * @param userId
	 *            selected userId
	 * @param destructive
	 *            flag to also remove user in KC.
	 * @return the list of actually removed user keys
	 */
	List<UserKey> remove(UUID pushApplicationId, UUID userId, boolean destructive);

	List<UserKey> remove(UUID pushApplicationId, UUID userId);

	void removeAll(PushApplication pushApplication, boolean destructive, PostDelete action);

	void create(Alias alias);

	void createAsynchronous(Alias alias);

	int updateKCUsersGuids();

	Set<UserTenantInfo> getTenantRelations(String alias);
}