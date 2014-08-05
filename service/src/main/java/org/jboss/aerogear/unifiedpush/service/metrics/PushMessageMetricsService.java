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
package org.jboss.aerogear.unifiedpush.service.metrics;

import org.jboss.aerogear.unifiedpush.api.PushMessageInformation;
import org.jboss.aerogear.unifiedpush.dao.PageResult;
import org.jboss.aerogear.unifiedpush.dao.PushMessageInformationDao;
import org.jboss.aerogear.unifiedpush.utils.DateUtils;

import javax.inject.Inject;
import java.util.Date;

/**
 * Service class to handle different aspects of the Push Message Information metadata for the "Push Message History" view
 * on the Admin UI.
 */
public class PushMessageMetricsService {

    // that's what we currently use as the maximum days the message information objects are stored
    // before wiped out automatically by the DeleteOldPushMessageInformationScheduler
    private final static int DAYS_OF_MAX_OLDEST_INFO_MSG = 30;

    @Inject
    private PushMessageInformationDao pushMessageInformationDao;

    /**
     * Starts the capturing of metadata around a push message request.
     *
     * @param pushAppId the ip of the push application which is owing the push message job
     * @param json the raw JSON data
     * @param ipAddress remote address of the job submitter
     * @param clientIdentifier the String representating who triggered the push message
     *
     * @return the metadata object for the started push message request job
     */
    public PushMessageInformation storeNewRequestFrom(String pushAppId, String json, String ipAddress, String clientIdentifier) {
        final PushMessageInformation information = new PushMessageInformation();

        information.setRawJsonMessage(json);
        information.setIpAddress(ipAddress);
        information.setPushApplicationId(pushAppId);
        information.setClientIdentifier(clientIdentifier);

        pushMessageInformationDao.create(information);

        return information;
    }


    /**
     * Delegates a database update for the given {@link org.jboss.aerogear.unifiedpush.api.PushMessageInformation} object.
     */
    public void updatePushMessageInformation(PushMessageInformation pushMessageInformation) {
        pushMessageInformationDao.update(pushMessageInformation);
    }

    /**
     * Returns a list of metadata objects for the given Push Application
     */
    public PageResult<PushMessageInformation> findAllForPushApplication(String pushApplicationID, boolean sorting, Integer page, Integer pageSize) {
        return pushMessageInformationDao.findAllForPushApplication(pushApplicationID, sorting, page, pageSize);
    }

    /**
     * Returns a list of metadata objects for the given Variant
     */
    public PageResult<PushMessageInformation> findAllForVariant(String variantID, boolean sorting, Integer page, Integer pageSize) {
        return pushMessageInformationDao.findAllForVariant(variantID, sorting, page, pageSize);
    }

    /**
     *  We trigger a delete of all {@link org.jboss.aerogear.unifiedpush.api.PushMessageInformation} objects that are
     *  <i>older</i> than 30 days!
     */
    public void deleteOutdatedPushInformationData() {
        final Date historyDate = DateUtils.calculatePastDate(DAYS_OF_MAX_OLDEST_INFO_MSG);
        pushMessageInformationDao.deletePushInformationOlderThan(historyDate);
    }
}
