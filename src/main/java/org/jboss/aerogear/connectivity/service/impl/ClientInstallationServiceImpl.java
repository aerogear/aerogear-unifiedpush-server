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
package org.jboss.aerogear.connectivity.service.impl;

import java.util.List;

import javax.inject.Inject;

import org.jboss.aerogear.connectivity.jpa.dao.InstallationDao;
import org.jboss.aerogear.connectivity.model.InstallationImpl;
import org.jboss.aerogear.connectivity.service.ClientInstallationService;

public class ClientInstallationServiceImpl implements ClientInstallationService {

    @Inject
    private InstallationDao dao;

    public InstallationImpl addInstallation(InstallationImpl installation) {
        return dao.create(installation);
    }

    @Override
    public void removeInstallations(
            List<InstallationImpl> installations) {

        // uh... :)
        for (InstallationImpl installation : installations) {
            removeInstallation(installation);
        }
    }

    @Override
    public InstallationImpl updateInstallation(
            InstallationImpl installation) {
        return dao.update(installation);
    }

    @Override
    public InstallationImpl updateInstallation(InstallationImpl installationToUpdate, InstallationImpl postedInstallation) {
        // copy the "updateable" values:
        installationToUpdate.setCategory(postedInstallation.getCategory());
        installationToUpdate.setDeviceToken(postedInstallation.getDeviceToken());
        installationToUpdate.setAlias(postedInstallation.getAlias());
        installationToUpdate.setDeviceType(postedInstallation.getDeviceType());
        installationToUpdate.setMobileOperatingSystem(postedInstallation
                .getMobileOperatingSystem());
        installationToUpdate.setOsVersion(postedInstallation.getOsVersion());

        // update it:
        return updateInstallation(installationToUpdate);
    }
    @Override
    public InstallationImpl findById(String primaryKey) {
        return dao.find(InstallationImpl.class, primaryKey);
    }

    @Override
    public void removeInstallation(InstallationImpl installation) {
        dao.delete(installation);
	}

    @Override
	public List<InstallationImpl> findInstallationsForVariantByDeviceToken(String variantID, String deviceToken) {
        return dao.findInstallationsForVariantByDeviceToken(variantID, deviceToken);
    }

    // =====================================================================
    // ======== Various finder services for the Sender REST API ============
    // =====================================================================

    @Override
    public List<String> findAllDeviceTokenForVariantID(String variantID) {
        return dao.findAllDeviceTokenForVariantIDByCategoryAndAliasAndDeviceType(variantID, null, null, null);
    }

    @Override
    public List<String> findAllDeviceTokenForVariantIDByCategory(String variantID, String category) {
        return dao.findAllDeviceTokenForVariantIDByCategoryAndAliasAndDeviceType(variantID, category, null, null);
    }

    @Override
    public List<String> findAllDeviceTokenForVariantIDByAliasAndDeviceType(String variantID, List<String> aliases, List<String> deviceTypes) {
        return dao.findAllDeviceTokenForVariantIDByCategoryAndAliasAndDeviceType(variantID, null, aliases, deviceTypes);
    }

    @Override
    public List<String> findAllDeviceTokenForVariantIDByCategoryAndAlias(String variantID, String category, List<String> aliases) {
        return dao.findAllDeviceTokenForVariantIDByCategoryAndAliasAndDeviceType(variantID, category, aliases, null);
    }
}
