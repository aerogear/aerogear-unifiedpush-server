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

import org.jboss.aerogear.unifiedpush.api.Installation;
import org.jboss.aerogear.unifiedpush.api.Variant;
import org.jboss.aerogear.unifiedpush.dao.ResultsStream;

/**
 * Service class used by the Server to work with Installations
 * for the different Variants.
 */
public interface ClientInstallationService {

    /**
     * Store a new Installation object on the database.
     */
    void addInstallation(Variant variant, Installation installation);

    /**
     * Add new Installations objects, for importing devices on the database.
     */
    void addInstallations(Variant variant, List<Installation> installations);

    /**
     * Performs an update/merge on the given entity.
     */
    void updateInstallation(Installation installation);

    /**
     * Updates the first argument with the values from the second, and returns the updated entity.
     * @param toUpdate entity to be updated
     * @param postedInstallation entity where we read the "updateable" values from.
     * @return updated entity
     */
    void updateInstallation(Installation toUpdate, Installation postedInstallation);

    /**
     * Returns the Installation entity, matching the given primaryKey.
     */
    Installation findById(String primaryKey);

    /**
     * Removes the given installation entity.
     */
    void removeInstallation(Installation installation);

    /**
     * Removes all the installation entities in the {@link List}.
     */
    void removeInstallations(List<Installation> installations);

    /**
     * Used for "feedback service": Collects the invalid Installations for a Variant, based on the identifier tokens:
     */
    void removeInstallationsForVariantByDeviceTokens(String variantID, Set<String> deviceTokens);

    /**
     * Used to remove single device token from UPS. Used for error handling of specific tokens
     */
    void removeInstallationForVariantByDeviceToken(String variantID, String deviceToken);

    /**
     * Used for "Device Registration":
     *
     * Finder that returns the actual client installation, identified by its device-token, for the given variant.
     */
    Installation findInstallationForVariantByDeviceToken(String variantID, String deviceToken);

    // ===================   SENDER API   ===================

    /**
     * Used for (Android/iOS) Sender API. Queries the available device-tokens for a given variant, based on provided criteria
     */
    ResultsStream.QueryBuilder<String> findAllDeviceTokenForVariantIDByCriteria(String variantID, List<String> categories, List<String> aliases, List<String> deviceTypes, int maxResults, String lastTokenFromPreviousBatch);

}
