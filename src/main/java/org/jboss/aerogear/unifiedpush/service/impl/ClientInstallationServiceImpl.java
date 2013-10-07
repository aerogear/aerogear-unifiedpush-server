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

import org.jboss.aerogear.unifiedpush.jpa.dao.InstallationDao;
import org.jboss.aerogear.unifiedpush.model.InstallationImpl;
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

    // The allowed SimplePush "device types
    // TODO: right now there is no 'auto' registration for SP clients that
    // store 'web' as their device type metadata. That is the reason for null.
    // Once that happens the null goes away
    private static final List<String> SIMPLE_PUSH_DEVICE_TYPES = null;
    //private static final List<String> SIMPLE_PUSH_DEVICE_TYPES = Arrays.asList("web");

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
        installationToUpdate.setOperatingSystem(postedInstallation
                .getOperatingSystem());
        installationToUpdate.setOsVersion(postedInstallation.getOsVersion());
        installationToUpdate.setSimplePushEndpoint(postedInstallation.getSimplePushEndpoint());
        installationToUpdate.setEnabled(postedInstallation.isEnabled());
        installationToUpdate.setPlatform(postedInstallation.getPlatform());

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
    @Asynchronous
    public void removeInstallationsForVariantByDeviceTokens(String variantID, Set<String> deviceTokens) {
        // collect inactive installations for the given variant:
        List<InstallationImpl> inactiveInstallations = dao.findInstallationsForVariantByDeviceTokens(variantID, deviceTokens);
        // get rid of them
        this.removeInstallations(inactiveInstallations);
    }

    @Override
    public InstallationImpl findInstallationForVariantByDeviceToken(String variantID, String deviceToken) {
        return dao.findInstallationForVariantByDeviceToken(variantID, deviceToken);
    }

    // =====================================================================
    // ======== Various finder services for the Sender REST API ============
    // =====================================================================

    /**
     * For selective send (Android / iOS)
     */
    @Override
    public List<String> findAllDeviceTokenForVariantIDByCriteria(String variantID, String category, List<String> aliases, List<String> deviceTypes) {
        return dao.findAllDeviceTokenForVariantIDByCriteria(variantID, category, aliases, deviceTypes);
    }

    /**
     * For selective send  (SimplePush)
     */
    @Override
    public List<String> findAllSimplePushEndpointURLsForVariantIDByCriteria(String variantID, String simplePushCategory, List<String> aliases) {
        return dao.findAllPushEndpointURLsForVariantIDByCriteria(variantID, simplePushCategory, aliases, SIMPLE_PUSH_DEVICE_TYPES);
    }
}
