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

import org.jboss.aerogear.unifiedpush.api.Installation;
import org.jboss.aerogear.unifiedpush.api.Variant;
import org.jboss.aerogear.unifiedpush.dao.InstallationDao;
import org.jboss.aerogear.unifiedpush.dao.PageResult;
import org.jboss.aerogear.unifiedpush.service.ClientInstallationService;

import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.List;
import java.util.Set;

/**
 * (Default) implementation of the {@code ClientInstallationService} interface.
 * Delegates work to an injected DAO object.
 */
@Stateless
public class ClientInstallationServiceImpl implements ClientInstallationService {

    @Inject
    private InstallationDao dao;

    public void addInstallation(Variant variant, Installation installation) {
        installation.setVariant(variant);
        dao.create(installation);
    }

    @Override
    public void removeInstallations(
            List<Installation> installations) {

        // uh... :)
        for (Installation installation : installations) {
            removeInstallation(installation);
        }
    }

    @Override
    public PageResult<Installation> findInstallationsByVariant(String variantId, String developer, Integer page, Integer pageSize) {
        return dao.findInstallationsByVariant(variantId, developer, page, pageSize);
    }

    @Override
    public void updateInstallation(
            Installation installation) {
        dao.update(installation);
    }

    @Override
    public void updateInstallation(Installation installationToUpdate, Installation postedInstallation) {
        // copy the "updateable" values:
        installationToUpdate.setCategories(postedInstallation.getCategories());
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
        return dao.find(primaryKey);
    }

    @Override
    public void removeInstallation(Installation installation) {
        dao.delete(installation);
    }

    @Override
    @Asynchronous
    public void removeInstallationsForVariantByDeviceTokens(String variantID, Set<String> deviceTokens) {
        // collect inactive installations for the given variant:
        List<Installation> inactiveInstallations = dao.findInstallationsForVariantByDeviceTokens(variantID, deviceTokens);
        // get rid of them
        this.removeInstallations(inactiveInstallations);
    }

    @Override
    public Installation findInstallationForVariantByDeviceToken(String variantID, String deviceToken) {
        return dao.findInstallationForVariantByDeviceToken(variantID, deviceToken);
    }

    // =====================================================================
    // ======== Various finder services for the Sender REST API ============
    // =====================================================================

    /**
     * Finder for 'send', used for Android, iOS and SimplePush clients
     */
    @Override
    public List<String> findAllDeviceTokenForVariantIDByCriteria(String variantID, List<String> categories, List<String> aliases, List<String> deviceTypes) {
        return dao.findAllDeviceTokenForVariantIDByCriteria(variantID, categories, aliases, deviceTypes);
    }
}
