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
package org.jboss.aerogear.unifiedpush.service.impl;

import org.jboss.aerogear.unifiedpush.api.Category;
import org.jboss.aerogear.unifiedpush.api.Installation;
import org.jboss.aerogear.unifiedpush.api.Variant;
import org.jboss.aerogear.unifiedpush.dao.CategoryDao;
import org.jboss.aerogear.unifiedpush.dao.InstallationDao;
import org.jboss.aerogear.unifiedpush.dao.ResultsStream;
import org.jboss.aerogear.unifiedpush.service.ClientInstallationService;
import org.jboss.aerogear.unifiedpush.service.annotations.LoggedIn;
import org.jboss.aerogear.unifiedpush.utils.AeroGearLogger;

import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * (Default) implementation of the {@code ClientInstallationService} interface.
 * Delegates work to an injected DAO object.
 */
@Stateless
public class ClientInstallationServiceImpl implements ClientInstallationService {

    private final AeroGearLogger logger = AeroGearLogger.getInstance(ClientInstallationServiceImpl.class);

    @Inject
    private InstallationDao installationDao;

    @Inject
    private CategoryDao categoryDao;

    @Inject
    @LoggedIn
    private Instance<String> developer;

    @Override
    @Asynchronous
    public void addInstallation(Variant variant, Installation entity) {

        // does it already exist ?
        Installation installation = this.findInstallationForVariantByDeviceToken(variant.getVariantID(), entity.getDeviceToken());

        // Needed for the Admin UI Only. Help for setting up Routes
        entity.setPlatform(variant.getType().getTypeName());

        // new device/client ?
        if (installation == null) {
            logger.finest("Performing new device/client registration");
            // store the installation:
            entity.setVariant(variant);
            mergeCategories(entity);

            installationDao.create(entity);
        } else {
            // We only update the metadata, if the device is enabled:
            if (installation.isEnabled()) {
                logger.finest("Updating received metadata for an 'enabled' installation");
                // update the entity:
                this.updateInstallation(installation, entity);
            }
        }
    }

    @Override
    @Asynchronous
    public void addInstallations(Variant variant, List<Installation> installations) {

        // don't bother
        if (installations == null || installations.isEmpty()) {
            return;
        }

        Set<String> existingTokens = installationDao.findAllDeviceTokenForVariantID(variant.getVariantID());

        // clear out:
        installationDao.flushAndClear();

        for (int i = 0; i < installations.size(); i++) {

            Installation current = installations.get(i);

            // let's avoid duplicated tokens/devices per variant
            // For devices without a token, let's also not bother the DAO layer to throw BeanValidation exception
            if (!existingTokens.contains(current.getDeviceToken()) && hasTokenValue(current)) {

                logger.finest("Importing device with token: " + current.getDeviceToken());

                // set reference
                current.setVariant(variant);

                mergeCategories(current);
                installationDao.create(current);

                // and add a reference to the existing tokens set, to ensure the JSON file contains no duplicates:
                existingTokens.add(current.getDeviceToken());

                // some tunings, ever 10k devices releasing resources
                if (i % 10000 == 0) {
                    logger.finest("releasing some resources during import");
                    installationDao.flushAndClear();
                }
            } else {
                // for now, we ignore them.... no update applied!
                logger.finest("Device with token '" + current.getDeviceToken() + "' already exists. Ignoring it ");
            }
        }
        // clear out:
        installationDao.flushAndClear();
    }

    @Override
    public void removeInstallations(
            List<Installation> installations) {

        // uh... :)
        for (Installation installation : installations) {
            removeInstallation(installation);
        }
    }

    public void updateInstallation(
            Installation installation) {
        installationDao.update(installation);
    }

    @Override
    public void updateInstallation(Installation installationToUpdate, Installation postedInstallation) {
        // copy the "updateable" values:
        installationToUpdate.setCategories(postedInstallation.getCategories());
        mergeCategories(installationToUpdate);
        installationToUpdate.setDeviceToken(postedInstallation.getDeviceToken());
        installationToUpdate.setAlias(postedInstallation.getAlias());
        installationToUpdate.setDeviceType(postedInstallation.getDeviceType());
        installationToUpdate.setOperatingSystem(postedInstallation
                .getOperatingSystem());
        installationToUpdate.setOsVersion(postedInstallation.getOsVersion());
        installationToUpdate.setEnabled(postedInstallation.isEnabled());
        installationToUpdate.setPlatform(postedInstallation.getPlatform());

        // update it:
        updateInstallation(installationToUpdate);
    }

    @Override
    public Installation findById(String primaryKey) {
        return installationDao.find(primaryKey);
    }

    @Override
    public void removeInstallation(Installation installation) {
        installationDao.delete(installation);
    }

    @Override
    @Asynchronous
    public void removeInstallationsForVariantByDeviceTokens(String variantID, Set<String> deviceTokens) {
        // collect inactive installations for the given variant:
        List<Installation> inactiveInstallations = installationDao.findInstallationsForVariantByDeviceTokens(variantID, deviceTokens);
        // get rid of them
        this.removeInstallations(inactiveInstallations);
    }

    @Override
    @Asynchronous
    public void removeInstallationForVariantByDeviceToken(String variantID, String deviceToken) {
        removeInstallation(findInstallationForVariantByDeviceToken(variantID, deviceToken));
    }

    @Override
    public Installation findInstallationForVariantByDeviceToken(String variantID, String deviceToken) {
        return installationDao.findInstallationForVariantByDeviceToken(variantID, deviceToken);
    }

    // =====================================================================
    // ======== Various finder services for the Sender REST API ============
    // =====================================================================

    /**
     * Finder for 'send', used for Android, iOS and SimplePush clients
     */
    @Override
    public ResultsStream.QueryBuilder<String> findAllDeviceTokenForVariantIDByCriteria(String variantID, List<String> categories, List<String> aliases, List<String> deviceTypes, int maxResults, String lastTokenFromPreviousBatch) {
        return installationDao.findAllDeviceTokenForVariantIDByCriteria(variantID, categories, aliases, deviceTypes, maxResults, lastTokenFromPreviousBatch);
    }

    /**
     * A simple validation util that checks if a token is present
     */
    private boolean hasTokenValue(Installation installation) {
        return (installation.getDeviceToken() != null && (!installation.getDeviceToken().isEmpty()));
    }

    /**
     * When an installation is created or updated, the categories are passed without IDs.
     * This method solve this issue by checking for existing categories and updating them (otherwise it would
     * persist a new object).
     * @param entity
     */
    private void mergeCategories(Installation entity) {
        if (entity.getCategories() != null) {
            final List<String> categoryNames = convertToNames(entity.getCategories());
            final List<Category> categories = categoryDao.findByNames(categoryNames);
            //Replace json deserialised categories with their persistent counter parts see Category.equals
            entity.getCategories().removeAll(categories);
            entity.getCategories().addAll(categories);
        }
    }

    private List<String> convertToNames(Set<Category> categories) {
        List<String> result = new ArrayList<String>();
        for (Category category : categories) {
            result.add(category.getName());
        }
        return result;
    }
}
