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

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.jboss.aerogear.unifiedpush.api.Alias;
import org.jboss.aerogear.unifiedpush.api.AndroidVariant;
import org.jboss.aerogear.unifiedpush.api.Category;
import org.jboss.aerogear.unifiedpush.api.Installation;
import org.jboss.aerogear.unifiedpush.api.PushApplication;
import org.jboss.aerogear.unifiedpush.api.Variant;
import org.jboss.aerogear.unifiedpush.api.VariantType;
import org.jboss.aerogear.unifiedpush.dao.CategoryDao;
import org.jboss.aerogear.unifiedpush.dao.InstallationDao;
import org.jboss.aerogear.unifiedpush.dao.PushApplicationDao;
import org.jboss.aerogear.unifiedpush.dao.ResultsStream;
import org.jboss.aerogear.unifiedpush.service.AliasService;
import org.jboss.aerogear.unifiedpush.service.ClientInstallationService;
import org.jboss.aerogear.unifiedpush.service.VerificationService;
import org.jboss.aerogear.unifiedpush.service.util.FCMTopicManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * (Default) implementation of the {@code ClientInstallationService} interface.
 * Delegates work to an injected DAO object.
 */
@Service
@Transactional
public class ClientInstallationServiceImpl implements ClientInstallationService {
	private final Logger logger = LoggerFactory.getLogger(ClientInstallationServiceImpl.class);

	@Inject
	private InstallationDao installationDao;

	@Inject
	private CategoryDao categoryDao;

	@Inject
	private AliasService aliasService;

	@Inject
	private PushApplicationDao pushApplicationDao;

	@Inject
	private VerificationService verificationService;
	
	@Override
	public Variant associateInstallation(Installation installation, Variant currentVariant) {
		if (installation.getAlias() == null) {
			logger.warn("Unable to associate, installation alias is missing!");
			return null;
		}

		Alias alias = aliasService.find(null, installation.getAlias());

		if (alias == null) {
			return null;
		}

		PushApplication application = pushApplicationDao
				.findByPushApplicationID(alias.getPushApplicationId().toString());
		if (application == null) {
			logger.warn(String.format(
					"Unable to find application for alias %s, this behaviour "
							+ "might occur when application is deleted and orphans aliases exists. "
							+ "Use DELETE /rest/alias/THE-ALIAS in order to remove orphans.",
					StringUtils.isEmpty(alias.getEmail()) ? alias.getOther() : alias.getEmail()));
			return null;
		}

		List<Variant> variants = application.getVariants();

		for (Variant variant : variants) {
			// Match variant type according to previous variant.
			if (variant.getType().equals(currentVariant.getType())) {
				installation.setVariant(variant);
				updateInstallation(installation);
				return variant;
			}
		}

		// TODO - Make sure user is associated to a KC client.
		// If not, associate to appropriate rules.

		return null;
	}

	@Override
	public void addInstallation(Variant variant, Installation entity) {
		addInstallation(variant, entity, true);
	}

	/**
	 * TODO - Once only KC based registration are publised (android/ios) we can
	 * remove shouldVerifiy condition.
	 */
	public void addInstallation(Variant variant, Installation entity, boolean shouldVerifiy) {

		// does it already exist ?
		Installation installation = this.findInstallationForVariantByDeviceToken(variant.getVariantID(),
				entity.getDeviceToken());

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

		// TODO - Remove according to top comment
		if (shouldVerifiy)
			verificationService.initiateDeviceVerification(entity, variant);
	}

	@Override
	public void addInstallations(Variant variant, List<Installation> installations) {

		// don't bother
		if (installations == null || installations.isEmpty()) {
			return;
		}

		// TODO - On a large scale database, we can't load entire device list.
		Set<String> existingTokens = installationDao.findAllDeviceTokenForVariantID(variant.getVariantID());

		// clear out:
		installationDao.flushAndClear();

		for (int i = 0; i < installations.size(); i++) {

			Installation current = installations.get(i);

			// let's avoid duplicated tokens/devices per variant
			// For devices without a token, let's also not bother the DAO layer
			// to throw BeanValidation exception
			if (!existingTokens.contains(current.getDeviceToken()) && hasTokenValue(current)) {

				logger.trace("Importing device with token: {}", current.getDeviceToken());

				storeInstallationAndSetReferences(variant, current);

				// and add a reference to the existing tokens set, to ensure the
				// JSON file contains no duplicates:
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
	}

	@Override
	public void removeInstallations(List<Installation> installations) {
		// uh..., fancy method reference :)
		installations.forEach(this::removeInstallation);
	}

	@Override
	public void updateInstallation(Installation installation) {
		installationDao.update(installation);
	}

	@Override
	public void updateInstallation(Installation installationToUpdate, Installation postedInstallation) {
		// copy the "updateable" values:
		mergeCategories(installationToUpdate, postedInstallation.getCategories());

		installationToUpdate.setDeviceToken(postedInstallation.getDeviceToken());
		installationToUpdate.setAlias(postedInstallation.getAlias());
		installationToUpdate.setDeviceType(postedInstallation.getDeviceType());
		installationToUpdate.setOperatingSystem(postedInstallation.getOperatingSystem());
		installationToUpdate.setOsVersion(postedInstallation.getOsVersion());
		installationToUpdate.setEnabled(postedInstallation.isEnabled());
		installationToUpdate.setPlatform(postedInstallation.getPlatform());

		// update it:
		updateInstallation(installationToUpdate);

		// unsubscribe Android devices from topics that device should no longer
		// be subscribed to
		if (installationToUpdate.getVariant().getType() == VariantType.ANDROID) {
			unsubscribeOldTopics(installationToUpdate);
		}
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
	public void removeInstallationsForVariantByDeviceTokens(String variantID, Set<String> deviceTokens) {
		// collect inactive installations for the given variant:
		List<Installation> inactiveInstallations = installationDao.findInstallationsForVariantByDeviceTokens(variantID,
				deviceTokens);
		// get rid of them
		this.removeInstallations(inactiveInstallations);
	}

	@Override
	public void removeInstallationForVariantByDeviceToken(String variantID, String deviceToken) {
		removeInstallation(findInstallationForVariantByDeviceToken(variantID, deviceToken));
	}

	@Override
	public Installation findInstallationForVariantByDeviceToken(String variantID, String deviceToken) {
		return installationDao.findInstallationForVariantByDeviceToken(variantID, deviceToken);
	}

	@Override
	public void unsubscribeOldTopics(Installation installation) {
		FCMTopicManager topicManager = new FCMTopicManager((AndroidVariant) installation.getVariant());
		Set<String> oldCategories = topicManager.getSubscribedCategories(installation);
		// Remove current categories from the set of old ones
		oldCategories.removeAll(convertToNames(installation.getCategories()));

		// Remove global variant topic because we don't want to unsubscribe it
		oldCategories.remove(installation.getVariant().getVariantID());

		for (String categoryName : oldCategories) {
			topicManager.unsubscribe(installation, categoryName);
		}
	}

	// =====================================================================
	// ======== Various finder services for the Sender REST API ============
	// =====================================================================

	/**
	 * Finder for 'send', used for Android, iOS and SimplePush clients
	 */
	@Override
	public ResultsStream.QueryBuilder<String> findAllDeviceTokenForVariantIDByCriteria(String variantID,
			List<String> categories, List<String> aliases, List<String> deviceTypes, int maxResults,
			String lastTokenFromPreviousBatch) {
		return installationDao.findAllDeviceTokenForVariantIDByCriteria(variantID, categories, aliases, deviceTypes,
				maxResults, lastTokenFromPreviousBatch, false);
	}

	@Override
	public ResultsStream.QueryBuilder<String> findAllOldGoogleCloudMessagingDeviceTokenForVariantIDByCriteria(
			String variantID, List<String> categories, List<String> aliases, List<String> deviceTypes, int maxResults,
			String lastTokenFromPreviousBatch) {
		return installationDao.findAllDeviceTokenForVariantIDByCriteria(variantID, categories, aliases, deviceTypes,
				maxResults, lastTokenFromPreviousBatch, true);
	}

	/**
	 * A simple validation util that checks if a token is present
	 */
	private boolean hasTokenValue(Installation installation) {
		return installation.getDeviceToken() != null && !installation.getDeviceToken().isEmpty();
	}

	/**
	 * When an installation is created or updated, the categories are passed without
	 * IDs. This method solve this issue by checking for existing categories and
	 * updating them (otherwise it would persist a new object).
	 *
	 * @param entity            to merge the categories for
	 * @param categoriesToMerge are the categories to merge with the existing one
	 */
	private void mergeCategories(Installation entity, Set<Category> categoriesToMerge) {
		if (entity.getCategories() != null) {
			final List<String> categoryNames = convertToNames(categoriesToMerge);
			final List<Category> existingCategoriesFromDB = categoryDao.findByNames(categoryNames);

			// Replace json dematerialised categories with their persistent
			// counter parts (see Category.equals),
			// by remove existing/persistent categories from the new collection,
			// and adding them back in (with their PK).
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
	protected void storeInstallationAndSetReferences(Variant variant, Installation entity) {

		// ensure lower case for iOS
		if (variant.getType() == VariantType.IOS) {
			entity.setDeviceToken(entity.getDeviceToken().toLowerCase());
		}
		// set reference
		entity.setVariant(variant);
		// update attached categories
		mergeCategories(entity, entity.getCategories());
		// store Installation entity
		installationDao.create(entity);
	}

	@Override
	public void removeInstallations(String alias) {
		List<Installation> insts = installationDao.findInstallationsByAlias(alias);
		if (insts != null) {
			insts.forEach(item -> installationDao.delete(item));
		}
	}

	public List<Installation> findByAlias(String alias) {
		return installationDao.findInstallationsByAlias(alias);
	}

	public long getNumberOfDevicesForVariantID(String variantId) {
		return installationDao.getNumberOfDevicesForVariantID(variantId);
	}
}
