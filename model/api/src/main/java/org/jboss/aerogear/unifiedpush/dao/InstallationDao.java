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

    /**
     * Loads a specific installation for the given Variant, specified by the device token.
     */
    Installation findInstallationForVariantByDeviceToken(String variantID, String deviceToken);

    /**
     * Loads all installations matching the <code>Set</code> of deviceTokens, for the given Variant
     */
    List<Installation> findInstallationsForVariantByDeviceTokens(String variantID, Set<String> deviceTokens);

    /**
     * Sender API for installations:
     *
     * Query all tokens for the given variant, by respecting a few criteria arguments (categories, aliases and deviceTypes)
     */
    ResultsStream.QueryBuilder<String> findAllDeviceTokenForVariantIDByCriteria(String variantID, List<String> categories, List<String> aliases, List<String> deviceTypes, int maxResults, String lastTokenFromPreviousBatch);

    Set<String> findAllDeviceTokenForVariantID(String variantID);

    /**
     * Find all installations for the variant specified and for the authneticated user.
     * @param variantID the id of the variant to find the installations for
     * @param developer the developer
     * @param page the page number
     * @param pageSize the size of the pages
     * @return all installations found or empty list + the total count of results
     */
    PageResult<Installation> findInstallationsByVariantForDeveloper(String variantID, String developer, Integer page, Integer pageSize);

    /**
     * Find all installations for the variant specified (used for admin role)
     * @param variantID the id of the variant to find the installations for
     * @param page the page number
     * @param pageSize the size of the pages
     * @return all installations found or empty list + the total count of results
     */
    PageResult<Installation> findInstallationsByVariant(String variantID, Integer page, Integer pageSize);


    /**
     * Counts the total number of registered devices/clients for the give List of variantIDs
     */
    long getNumberOfDevicesForVariantIDs(String loginName);

    //Admin query
    /**
     * Counts the total number of all registered devices/clients
     */
    long getNumberOfDevicesForVariantIDs();
}
