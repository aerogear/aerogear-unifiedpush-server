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
import org.jboss.aerogear.unifiedpush.api.PushApplication;
import org.jboss.aerogear.unifiedpush.api.Variant;
import org.jboss.aerogear.unifiedpush.dao.ResultsStream;

/**
 * Service class used by the Server to work with Installations
 * for the different Variants.
 */
public interface ClientInstallationService {

    /**
     * Store a new Installation object on the database.
     *
     * @param variant the variant to store on
     * @param installation the installation
     */
    void addInstallation(Variant variant, Installation installation);

    void addInstallationSynchronously(Variant variant, Installation entity);
    /**
     * Add new Installations objects, for importing devices on the database.
     *
     * @param variant the variant to store on
     * @param installations list of installations
     */
    void addInstallations(Variant variant, List<Installation> installations);

    void addInstallationsSynchronously(Variant variant, List<Installation> installations);
    
    /**
     * Performs an update/merge on the given entity.
     *
     * @param installation the installation
     */
    void updateInstallation(Installation installation);

    /**
     * Updates the first argument with the values from the second, and returns the updated entity.
     *
     * @param toUpdate entity to be updated
     * @param postedInstallation entity where we read the "updateable" values from.
     */
    void updateInstallation(Installation toUpdate, Installation postedInstallation);

    /**
     * Returns the Installation entity, matching the given primaryKey.
     *
     * @param primaryKey the PK for the installation
     *
     * @return the installation entity
     */
    Installation findById(String primaryKey);

    /**
     * Removes the given installation entity.
     *
     * @param installation the installation
     */
    void removeInstallation(Installation installation);

    /**
     * Removes all the installation entities in the {@link List}.
     *
     * @param installations list of installations
     */
    void removeInstallations(List<Installation> installations);

    /**
     * Used for "feedback service": Collects the invalid Installations for a Variant, based on the identifier tokens.
     *
     * @param variantID id of the variant
     * @param deviceTokens list of tokens
     */
    void removeInstallationsForVariantByDeviceTokens(String variantID, Set<String> deviceTokens);

    /**
     * Used to remove single device token from UPS. Used for error handling of specific tokens
     *
     * @param variantID id of the variant
     * @param deviceToken one tokens
     */
    void removeInstallationForVariantByDeviceToken(String variantID, String deviceToken);

    void removeInstallationForVariantByDeviceTokenSynchronously(String variantID, String deviceToken);
    	
    /**
     * Used for "Device Registration":
     *
     * Finder that returns the actual client installation, identified by its device-token, for the given variant.
     *
     * @param variantID id of the variant
     * @param deviceToken one tokens
     *
     * @return the installation entity
     */
    Installation findInstallationForVariantByDeviceToken(String variantID, String deviceToken);

    // ===================   SENDER API   ===================

    /**
     * Used for (Android/iOS) Sender API. Queries the available device-tokens for a given variant, based on provided criteria.
     *
     * @param variantID the variantID for the filter
     * @param categories applied categories for the filter
     * @param aliases applied aliases for the filter
     * @param deviceTypes applied deviceTypes for the filter
     * @param maxResults number of maxResults for the filter
     * @param lastTokenFromPreviousBatch identifier of the last fetched token
     *
     * @return list of device tokens that matches this filter
     */
    ResultsStream.QueryBuilder<String> findAllDeviceTokenForVariantIDByCriteria(String variantID, List<String> categories, List<String> aliases, List<String> deviceTypes, int maxResults, String lastTokenFromPreviousBatch);

	Installation associateInstallation(Installation installation, Variant currentVariant);

	/**
	 * Removes installations that are installed under the supplied application, but whose alias
	 * does not match any alias in the {@code aliases} list.
	 * @param application the application the installations belong to
	 * @param aliases aliases to match against
	 */
	void removeInstallationNotInAliasList(PushApplication application, List<String> aliases);

}
