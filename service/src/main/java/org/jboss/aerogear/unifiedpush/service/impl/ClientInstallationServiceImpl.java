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
package org.jboss.aerogear.unifiedpush.service.impl;

import org.jboss.aerogear.unifiedpush.api.AndroidVariant;
import org.jboss.aerogear.unifiedpush.api.Category;
import org.jboss.aerogear.unifiedpush.api.Installation;
import org.jboss.aerogear.unifiedpush.api.Variant;
import org.jboss.aerogear.unifiedpush.api.VariantType;
import org.jboss.aerogear.unifiedpush.dao.CategoryDao;
import org.jboss.aerogear.unifiedpush.dao.InstallationDao;
import org.jboss.aerogear.unifiedpush.dao.ResultsStream;
import org.jboss.aerogear.unifiedpush.service.ClientInstallationService;
import org.jboss.aerogear.unifiedpush.service.annotations.LoggedIn;
import org.jboss.aerogear.unifiedpush.service.util.FCMTopicManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.AsyncResult;
import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * (Default) implementation of the {@code ClientInstallationService} interface.
 * Delegates work to an injected DAO object.
 */
@Stateless
public class ClientInstallationServiceImpl implements ClientInstallationService {

    private static final Logger logger = LoggerFactory.getLogger(ClientInstallationServiceImpl.class);

    @Inject
    private InstallationDao installationDao;

    @Inject
    private CategoryDao categoryDao;

    @Inject
    @LoggedIn
    private Instance<String> developer;

    @Override
    @Asynchronous
    public Future<Void> addInstallation(Variant variant, Installation entity) {

        // does it already exist ?
        Installation installation = this.findInstallationForVariantByDeviceToken(variant.getVariantID(), entity.getDeviceToken());

        // Needed for the Admin UI Only. Help for setting up Routes
        entity.setPlatform(variant.getType().getTypeName());

        // new device/client ?
        if (installation == null) {
            logger.trace("Performing new device/client registration");

            // store the installation:
            storeInstallationAndSetReferences(variant, entity);
        } else {
            // We only update the metadata, if the device is enabled:
            if (installation.isEnabled()) {
                logger.trace("Updating received metadata for an 'enabled' installation");

                // fix variant property of installation object
                installation.setVariant(variant);

                // update the entity:
                this.updateInstallation(installation, entity);
            }
        }
        return new AsyncResult<>(null);
    }

    @Override
    @Asynchronous
    public Future<Void>  addInstallations(Variant variant, List<Installation> installations) {

        // don't bother
        if (installations == null || installations.isEmpty()) {
            return new AsyncResult<>(null);
        }

        Set<String> existingTokens = installationDao.findAllDeviceTokenForVariantID(variant.getVariantID());

        // clear out:
        installationDao.flushAndClear();

        for (int i = 0; i < installations.size(); i++) {

            Installation current = installations.get(i);

            // let's avoid duplicated tokens/devices per variant
            // For devices without a token, let's also not bother the DAO layer to throw BeanValidation exception
            if (!existingTokens.contains(current.getDeviceToken()) && hasTokenValue(current)) {

                logger.trace("Importing device with token: {}", current.getDeviceToken());

                storeInstallationAndSetReferences(variant, current);

                // and add a reference to the existing tokens set, to ensure the JSON file contains no duplicates:
                existingTokens.add(current.getDeviceToken());

                // some tunings, ever 10k devices releasing resources
                if (i % 10000 == 0) {
                    logger.trace("releasing some resources during import");
                    installationDao.flushAndClear();
                }
            } else {
                // for now, we ignore them.... no update applied!
                logger.trace("Device with token '{}' already exists. Ignoring it ", current.getDeviceToken());
            }
        }
        // clear out:
        installationDao.flushAndClear();
        return new AsyncResult<>(null);
    }

    @Override
    public void  removeInstallations(
            List<Installation> installations) {

        // uh..., fancy method reference :)
        installations.forEach(this::removeInstallation);

    }

    @Override
    public void updateInstallation(
            Installation installation) {
        installationDao.update(installation);
    }

    @Override
    public void updateInstallation(Installation installationToUpdate, Installation postedInstallation) {
        // copy the "updateable" values:
        mergeCategories(installationToUpdate, postedInstallation.getCategories());

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

        // unsubscribe Android devices from topics that device should no longer be subscribed to
        if (installationToUpdate.getVariant().getType() == VariantType.ANDROID) {
            unsubscribeOldTopics(installationToUpdate);
        }
    }

    @Override
    public Installation findById(String primaryKey) {
        return installationDao.find(primaryKey);
    }

    @Override
    public void  removeInstallation(Installation installation) {
        installationDao.delete(installation);
    }

    @Override
    @Asynchronous
    public Future<Void>  removeInstallationsForVariantByDeviceTokens(String variantID, Set<String> deviceTokens) {
        // collect inactive installations for the given variant:
        List<Installation> inactiveInstallations = installationDao.findInstallationsForVariantByDeviceTokens(variantID, deviceTokens);
        // get rid of them
        this.removeInstallations(inactiveInstallations);
        return new AsyncResult<>(null);
    }

    @Override
    @Asynchronous
    public Future<Void>  removeInstallationForVariantByDeviceToken(String variantID, String deviceToken) {
        removeInstallation(findInstallationForVariantByDeviceToken(variantID, deviceToken));
        return new AsyncResult<>(null);
    }

    @Override
    public Installation findInstallationForVariantByDeviceToken(String variantID, String deviceToken) {
        return installationDao.findInstallationForVariantByDeviceToken(variantID, deviceToken);
    }

    @Override
    @Asynchronous
    public Future<Void>  unsubscribeOldTopics(Installation installation) {
        FCMTopicManager topicManager = new FCMTopicManager((AndroidVariant) installation.getVariant());
        Set<String> oldCategories = topicManager.getSubscribedCategories(installation);
        // Remove current categories from the set of old ones
        oldCategories.removeAll(convertToNames(installation.getCategories()));

        // Remove global variant topic because we don't want to unsubscribe it
        oldCategories.remove(installation.getVariant().getVariantID());

        for (String categoryName : oldCategories) {
            topicManager.unsubscribe(installation, categoryName);
        }
        return new AsyncResult<>(null);
    }

    // =====================================================================
    // ======== Various finder services for the Sender REST API ============
    // =====================================================================

    /**
     * Finder for 'send', used for Android and iOS clients
     */
    @Override
    public ResultsStream.QueryBuilder<String> findAllDeviceTokenForVariantIDByCriteria(String variantID, List<String> categories, List<String> aliases, List<String> deviceTypes, int maxResults, String lastTokenFromPreviousBatch) {
        return installationDao.findAllDeviceTokenForVariantIDByCriteria(variantID, categories, aliases, deviceTypes, maxResults, lastTokenFromPreviousBatch, false);
    }

    @Override
    public ResultsStream.QueryBuilder<String> findAllOldGoogleCloudMessagingDeviceTokenForVariantIDByCriteria(String variantID, List<String> categories, List<String> aliases, List<String> deviceTypes, int maxResults, String lastTokenFromPreviousBatch) {
        return installationDao.findAllDeviceTokenForVariantIDByCriteria(variantID, categories, aliases, deviceTypes, maxResults, lastTokenFromPreviousBatch, true);
    }

    /**
     * A simple validation util that checks if a token is present
     */
    private static boolean hasTokenValue(Installation installation) {
        return installation.getDeviceToken() != null && !installation.getDeviceToken().isEmpty();
    }

    /**
     * When an installation is created or updated, the categories are passed without IDs.
     * This method solve this issue by checking for existing categories and updating them (otherwise it would
     * persist a new object).
     * @param entity to merge the categories for
     * @param categoriesToMerge are the categories to merge with the existing one
     */
    private void mergeCategories(Installation entity, Set<Category> categoriesToMerge) {
        if (entity.getCategories() != null) {
            final List<String> categoryNames = convertToNames(categoriesToMerge);
            final List<Category> existingCategoriesFromDB = categoryDao.findByNames(categoryNames);

            // Replace json dematerialised categories with their persistent counter parts (see Category.equals),
            // by remove existing/persistent categories from the new collection, and adding them back in (with their PK).
            categoriesToMerge.removeAll(existingCategoriesFromDB);
            categoriesToMerge.addAll(existingCategoriesFromDB);

            // and apply the passed in ones.
            entity.setCategories(categoriesToMerge);
        }
    }

    private static List<String> convertToNames(Set<Category> categories) {
        return categories.stream().map(Category::getName).collect(Collectors.toList());
    }

    /*
     * Helper to set references and perform the actual storage
     */
    private void storeInstallationAndSetReferences(Variant variant, Installation entity) {

        // ensure lower case for iOS
        if (variant.getType() == VariantType.IOS || variant.getType() == VariantType.IOS_TOKEN) {
            entity.setDeviceToken(entity.getDeviceToken().toLowerCase());
        }
        // set reference
        entity.setVariant(variant);
        // update attached categories
        mergeCategories(entity, entity.getCategories());
        // store Installation entity
        installationDao.create(entity);
    }
}
