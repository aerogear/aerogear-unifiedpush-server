/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.unifiedpush.dao;

import org.jboss.aerogear.unifiedpush.api.Installation;

import java.util.List;
import java.util.Set;

public interface InstallationDao extends GenericBaseDao<Installation, String> {

    Installation findInstallationForVariantByDeviceToken(String variantID, String deviceToken);

    /**
     * Loads all installations matching the <code>Set</code> of deviceTokens, for the given Variant
     */
    List<Installation> findInstallationsForVariantByDeviceTokens(String variantID, Set<String> deviceTokens);

    /**
     * Sender API for native (Android/iOS) installations:
     *
     * Query all tokens for the given variant, by respecting a few criteria arguments (categories, aliases and deviceTypes)
     */
    List<String> findAllDeviceTokenForVariantIDByCriteria(String variantID, List<String> categories, List<String> aliases, List<String> deviceTypes);

    /**
     * Sender API for SimplePush installations:
     *
     * Query all pushEndpoint URLs for the given SimplePush variant, by respecting a few criteria arguments (categories, aliases and deviceTypes)
     */
    List<String> findAllPushEndpointURLsForVariantIDByCriteria(String variantID, List<String> categories, List<String> aliases, List<String> deviceTypes);

}
