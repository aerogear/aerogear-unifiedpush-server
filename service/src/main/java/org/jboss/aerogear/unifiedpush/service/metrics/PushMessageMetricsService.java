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

import org.jboss.aerogear.unifiedpush.api.FlatPushMessageInformation;
import org.jboss.aerogear.unifiedpush.api.Variant;
import org.jboss.aerogear.unifiedpush.api.VariantErrorStatus;
import org.jboss.aerogear.unifiedpush.dao.FlatPushMessageInformationDao;
import org.jboss.aerogear.unifiedpush.dao.PageResult;
import org.jboss.aerogear.unifiedpush.dto.MessageMetrics;
import org.jboss.aerogear.unifiedpush.system.ConfigurationUtils;
import org.jboss.aerogear.unifiedpush.utils.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service class to handle different aspects of the Push Message Information metadata for the "Push Message History" view
 * on the Admin UI.
 */
@Stateless
public class PushMessageMetricsService {

    private static final Logger logger = LoggerFactory.getLogger(PushMessageMetricsService.class);

    // system property name used as the configurable maximum days the message information objects are stored
    public static final String AEROGEAR_METRICS_STORAGE_MAX_DAYS = "aerogear.metrics.storage.days";

    @Inject
    private FlatPushMessageInformationDao flatPushMessageInformationDao;

    public FlatPushMessageInformation storeNewRequestFrom(String pushAppId, String json, String ipAddress, String clientIdentifier) {
        final FlatPushMessageInformation information = new FlatPushMessageInformation();

        information.setRawJsonMessage(json);
        information.setIpAddress(ipAddress);
        information.setPushApplicationId(pushAppId);
        information.setClientIdentifier(clientIdentifier);

        logger.trace("starting to track a new Push Message request in the database");
        flatPushMessageInformationDao.create(information);
        flatPushMessageInformationDao.flushAndClear();

        return information;
    }

    public void updatePushMessageInformation(FlatPushMessageInformation pushMessageInformation) {
        flatPushMessageInformationDao.update(pushMessageInformation);
    }

    public void appendError(final FlatPushMessageInformation pushMessageInformation, final Variant variant, final String errorMessage) {
        final VariantErrorStatus ves = new VariantErrorStatus(pushMessageInformation, variant, errorMessage);
        pushMessageInformation.getErrors().add(ves);
        try {
            flatPushMessageInformationDao.update(pushMessageInformation);
        } catch (Exception e) {
            logger.info("Failed to save pushMessageInformation: {}", e.getMessage());
            logger.debug("Details:", e);
        }
    }

    public PageResult<FlatPushMessageInformation, MessageMetrics> findAllFlatsForPushApplication(String pushApplicationID, String search, boolean sorting, Integer page, Integer pageSize) {
        return flatPushMessageInformationDao.findAllForPushApplication(pushApplicationID, search, sorting, page, pageSize);
    }

    /**
     * Returns number of push messages for given push application ID
     *
     * @param pushApplicationId the push app ID
     *
     * @return the number of message for the given push application
     */
    public long countMessagesForPushApplication(String pushApplicationId) {
        return flatPushMessageInformationDao.getNumberOfPushMessagesForPushApplication(pushApplicationId);
    }

    /**
     *  We trigger a delete of all {@link org.jboss.aerogear.unifiedpush.api.FlatPushMessageInformation} objects that are
     *  <i>older</i> than 30 days!
     */
    public void deleteOutdatedFlatPushInformationData() {
        final Date historyDate = DateUtils.calculatePastDate(ConfigurationUtils.tryGetGlobalIntegerProperty(AEROGEAR_METRICS_STORAGE_MAX_DAYS, 30));
        logger.trace("Delete all until {}", historyDate.getTime());
        flatPushMessageInformationDao.deletePushInformationOlderThan(historyDate);
    }

    public FlatPushMessageInformation getPushMessageInformation(String id) {
        return flatPushMessageInformationDao.find(id);
    }

    public void updateAnalytics(String aerogearPushId) {
        FlatPushMessageInformation pushMessageInformation = this.getPushMessageInformation(aerogearPushId);

        if (pushMessageInformation != null) { //if we are here, app has been opened due to a push message

            //if the firstOpenDate is not null that means it's no the first one, let's update the lastDateOpen
            if (pushMessageInformation.getFirstOpenDate() != null) {
                pushMessageInformation.setLastOpenDate(new Date());
            } else {
                pushMessageInformation.setFirstOpenDate(new Date());
                pushMessageInformation.setLastOpenDate(new Date());
            }
            //update the general counter
            logger.trace("Incrementing 'open counter' for Push Notification '{}' ", aerogearPushId);
            pushMessageInformation.incrementAppOpenCounter();
        }

    }
}
