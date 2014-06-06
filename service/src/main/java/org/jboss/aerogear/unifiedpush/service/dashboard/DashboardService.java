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
package org.jboss.aerogear.unifiedpush.service.dashboard;

import org.jboss.aerogear.unifiedpush.api.Variant;
import org.jboss.aerogear.unifiedpush.dao.InstallationDao;
import org.jboss.aerogear.unifiedpush.dao.PushApplicationDao;
import org.jboss.aerogear.unifiedpush.dao.PushMessageInformationDao;
import org.jboss.aerogear.unifiedpush.dao.VariantDao;

import javax.inject.Inject;
import java.util.List;

/**
 * Class for loading various data for the Dashboard of the Admin UI
 */
public class DashboardService {

    @Inject
    private PushApplicationDao pushApplicationDao;
    @Inject
    private VariantDao variantDao;
    @Inject
    private InstallationDao installationDao;
    @Inject
    private PushMessageInformationDao pushMessageInformationDao;


    /**
     * Receives the dashboard data for the given user
     */
    public DashboardData loadDashboardData(String principalName) {

        long totalApps = totalApplicationNumber(principalName);
        long totalDevices = totalDeviceNumber(principalName);
        long totalMessages = totalMessages(principalName);


        final DashboardData data = new DashboardData();
        data.setApplications(totalApps);
        data.setDevices(totalDevices);
        data.setMessages(totalMessages);

        return data;
    }

    /**
     * Loads all the Variant objects where we did notice some failures on sending
     * for the given user
     */
    public List<Variant> getVariantsWithWarninings(String principalName) {
        final List<String> variantIDs = getVariantIDsForDeveloper(principalName);
        final List<String> warningIDs = pushMessageInformationDao.findVariantIDsWithWarnings(variantIDs);

        return variantDao.findAllVariantsByIDs(warningIDs);
    }

    /**
     * Loads all the Variant objects with the most received messages
     */
    public List<Variant> getTopThreeBusyVariants(String principalName) {
        final List<String> variantIDs = getVariantIDsForDeveloper(principalName);
        final List<String> topVariantIDs = pushMessageInformationDao.findTopThreeBusyVariantIDs(variantIDs);

        return variantDao.findAllVariantsByIDs(topVariantIDs);
    }

    private long totalMessages(String principalName) {
        List<String> pushAppIDs = pushApplicationDao.findAllPushApplicationIDsForDeveloper(principalName);
        return pushMessageInformationDao.getNumberOfPushMessagesForApplications(pushAppIDs);
    }

    private long totalDeviceNumber(String principalName) {

        List<String> variantIDs = getVariantIDsForDeveloper(principalName);

        return installationDao.getNumberOfDevicesForVariantIDs(variantIDs);
    }

    private List<String> getVariantIDsForDeveloper(String principalName) {
        return variantDao.findVariantIDsForDeveloper(principalName);
    }

    private long totalApplicationNumber(String principalName) {
        return  pushApplicationDao.getNumberOfPushApplicationsForDeveloper(principalName);
    }
}
