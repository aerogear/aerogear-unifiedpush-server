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
import java.util.UUID;

import org.jboss.aerogear.unifiedpush.api.Alias;
import org.jboss.aerogear.unifiedpush.api.Installation;
import org.jboss.aerogear.unifiedpush.api.PushApplication;

public interface AliasService {
	/**
	 * Creates <b>distinct</b> (according to
	 * {@code {@link org.jboss.aerogear.unifiedpush.api.Alias#getEmail()}})
	 * aliases, mirroring the data received in {@code aliases}, and associates
	 * them to the given application. Note that this application's existing
	 * aliases will be overwritten by the newly created aliases.
	 *
	 * Any {@code Installation} that was previously installed under this
	 * application and whose alias does not match one of the aliases provided in
	 * {@code aliases} will be disabled.
	 *
	 * @param pushApplication
	 *            push application to associate the aliases to.
	 * @param aliases
	 *            aliases list to match against.
	 * @param oauth2
	 *            synchronize state to identity provider
	 * @return new {@link org.jboss.aerogear.unifiedpush.api.Alias} list
	 */
	List<Alias> syncAliases(PushApplication pushApplication, List<String> aliases, boolean oauth2);

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

	Installation exists(String alias);

	Alias create(String pushApplicationId, String alias);

	Alias find(String pushApplicationId, String alias);

	Alias find(UUID pushApplicationId, UUID userId);

	void remove(UUID pushApplicationId, String alias);

	void remove(UUID pushApplicationId, UUID userId);

	void removeAll(UUID pushApplicationId);

	void create(Alias alias);

	void createAsynchronous(Alias alias);

}