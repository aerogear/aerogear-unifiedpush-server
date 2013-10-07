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
package org.jboss.aerogear.unifiedpush.jpa.dao;

import java.util.List;
import java.util.Set;

import org.jboss.aerogear.unifiedpush.jpa.GenericDao;
import org.jboss.aerogear.unifiedpush.model.InstallationImpl;

/**
 * Data Access Object interface to manage different finders for the 
 * {@code InstallationImpl} entities.
 */
public interface InstallationDao extends GenericDao<InstallationImpl, String> {

    /**
     * Finder that returns the actual client installation, identified by its device-token, for the given variant.
     */
    InstallationImpl findInstallationForVariantByDeviceToken(String variantID, String deviceToken);

    /**
     * Loads all installations matching the <code>Set</code> of deviceTokens, for the given Variant
     */
    List<InstallationImpl> findInstallationsForVariantByDeviceTokens(String variantID, Set<String> deviceTokens);

    /**
     * Sender API for native (Android/iOS) installations:
     * 
     * Query all tokens for the given variant, by respecting a few criteria arguments (category, aliases and deviceTypes)
     */
    List<String> findAllDeviceTokenForVariantIDByCriteria(String variantID, String category, List<String> aliases, List<String> deviceTypes);

    /**
     * Sender API for SimplePush installations:
     * 
     * Query all pushEndpoint URLs for the given SimplePush variant, by respecting a few criteria arguments (category, aliases and deviceTypes)
     */
    List<String> findAllPushEndpointURLsForVariantIDByCriteria(String variantID, String category, List<String> aliases, List<String> deviceTypes);
}
