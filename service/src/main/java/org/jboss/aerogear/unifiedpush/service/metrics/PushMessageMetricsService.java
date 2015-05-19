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

import java.util.Date;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.jboss.aerogear.unifiedpush.api.PushMessageInformation;
import org.jboss.aerogear.unifiedpush.api.VariantMetricInformation;
import org.jboss.aerogear.unifiedpush.dao.PageResult;
import org.jboss.aerogear.unifiedpush.dao.PushMessageInformationDao;
import org.jboss.aerogear.unifiedpush.dao.VariantMetricInformationDao;
import org.jboss.aerogear.unifiedpush.dto.MessageMetrics;
import org.jboss.aerogear.unifiedpush.utils.DateUtils;

/**
 * Service class to handle different aspects of the Push Message Information metadata for the "Push Message History" view
 * on the Admin UI.
 */
@Stateless
public class PushMessageMetricsService {

    // that's what we currently use as the maximum days the message information objects are stored
    // before wiped out automatically by the DeleteOldPushMessageInformationScheduler
    private final static int DAYS_OF_MAX_OLDEST_INFO_MSG = 30;

    @Inject
    private PushMessageInformationDao pushMessageInformationDao;

    @Inject
    private VariantMetricInformationDao variantMetricInformationDao;

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
    public PushMessageInformation storeNewRequestFrom(String pushAppId, String json, String ipAddress, String clientIdentifier, int totalVariantCount) {
        final PushMessageInformation information = new PushMessageInformation();

        information.setRawJsonMessage(json);
        information.setIpAddress(ipAddress);
        information.setPushApplicationId(pushAppId);
        information.setClientIdentifier(clientIdentifier);
        information.setServedVariants(0);
        information.setTotalVariants(totalVariantCount);

        pushMessageInformationDao.create(information);
        pushMessageInformationDao.flushAndClear();

        return information;
    }


    /**
     * Delegates a database update for the given {@link org.jboss.aerogear.unifiedpush.api.PushMessageInformation} object.
     *
     * @param pushMessageInformation the push message info object
     */
    public void updatePushMessageInformation(PushMessageInformation pushMessageInformation) {
        pushMessageInformationDao.update(pushMessageInformation);
    }

    /**
     * Refreshes state of push message information from the database, returning current state as a result.
     * @param pushMessageInformation push message information to update
     * @return current database state for the entity
     */
    public PushMessageInformation refreshPushMessageInformation(PushMessageInformation pushMessageInformation) {
        return pushMessageInformationDao.refresh(pushMessageInformation);
    }

    /**
     * Locks the push message information for updates so that there will be no updates concurrently
     * @param pushMessageInformation push message information to lock
     */
    public void lock(PushMessageInformation pushMessageInformation) {
        pushMessageInformationDao.lock(pushMessageInformation);
    }

    /**
     * Returns a list of metadata objects for the given Push Application
     *
     * @param pushApplicationID the push app ID
     * @param sorting do we want sorting?
     * @param page number of the actual page in the pagination
     * @param pageSize number of items
     *
     * @return list of push message info objects
     */
    public PageResult<PushMessageInformation, MessageMetrics> findAllForPushApplication(String pushApplicationID, String search, boolean sorting, Integer page, Integer pageSize) {
        return pushMessageInformationDao.findAllForPushApplication(pushApplicationID, search, sorting, page, pageSize);
    }

    /**
     * Returns number of push messages for given push application ID
     *
     * @param pushApplicationId the push app ID
     *
     * @return the cumber of message for the given push application
     */
    public long countMessagesForPushApplication(String pushApplicationId) {
        return pushMessageInformationDao.getNumberOfPushMessagesForPushApplication(pushApplicationId);
    }

    /**
     * Returns number of push messages for given push variant ID
     *
     * @param variantId the variant ID
     *
     * @return the cumber of message for the given variant
     */
    long countMessagesForVariant(String variantId) {
        return pushMessageInformationDao.getNumberOfPushMessagesForVariant(variantId);
    }

    /**
     *  We trigger a delete of all {@link org.jboss.aerogear.unifiedpush.api.PushMessageInformation} objects that are
     *  <i>older</i> than 30 days!
     */
    public void deleteOutdatedPushInformationData() {
        final Date historyDate = DateUtils.calculatePastDate(DAYS_OF_MAX_OLDEST_INFO_MSG);
        pushMessageInformationDao.deletePushInformationOlderThan(historyDate);
    }

    public PushMessageInformation getPushMessageInformation(String id) {
        return pushMessageInformationDao.find(id);
    }

    public void updateAnalytics(String aerogearPushId, String variantID) {
        PushMessageInformation pushMessageInformation = this.getPushMessageInformation(aerogearPushId);

        if (pushMessageInformation != null) { //if we are here, app has been opened due to a push message

            //if the firstOpenDate is not null that means it's no the first one, let's update the lastDateOpen
            if (pushMessageInformation.getFirstOpenDate() != null) {
                pushMessageInformation.setLastOpenDate(new Date());
            } else {
                pushMessageInformation.setFirstOpenDate(new Date());
                pushMessageInformation.setLastOpenDate(new Date());
            }
            //update the general counter
            pushMessageInformation.incrementAppOpenCounter();

            //update the variant counter
            VariantMetricInformation variantMetricInformation = variantMetricInformationDao.findVariantMetricInformationByVariantID(variantID, pushMessageInformation.getId());
            variantMetricInformation.incrementVariantOpenCounter();
            variantMetricInformationDao.update(variantMetricInformation);

            this.updatePushMessageInformation(pushMessageInformation);
        }

    }
}
