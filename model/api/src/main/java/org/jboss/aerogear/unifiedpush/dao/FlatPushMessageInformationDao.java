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
package org.jboss.aerogear.unifiedpush.dao;

import org.jboss.aerogear.unifiedpush.api.FlatPushMessageInformation;
import org.jboss.aerogear.unifiedpush.dto.MessageMetrics;

import java.util.Date;
import java.util.List;

public interface FlatPushMessageInformationDao extends GenericBaseDao<FlatPushMessageInformation, String>  {

    /**
     * Does a count for all the push message that have been submitted for the given PushApplication.
     *
     * @param loginName the username
     *
     * @return number of push messages
     */
    long getNumberOfPushMessagesForLoginName(String loginName);

    /**
     * Counts push messages for given push application ID
     *
     * @param pushApplicationId the push application ID
     *
     * @return number of push messages
     */
    long getNumberOfPushMessagesForPushApplication(String pushApplicationId);

    /**
     * Loads all push message metadata objects for the given PushApplication, but offers a way to order (asc/desc) by date.
     *
     * @param pushApplicationId ID of the PushApplication
     * @param ascending boolean to define if ASC ordering (by date) or DESC ordering should be executed.
     *
     * @return list of push message info objects
     */
    List<FlatPushMessageInformation> findAllForPushApplication(String pushApplicationId, boolean ascending);

    /**
     * Loads all push message metadata objects for the given PushApplication, but offers a way to order (asc/desc) by date.
     *
     * @param pushApplicationId ID of the PushApplication
     * @param ascending boolean to define if ASC ordering (by date) or DESC ordering should be executed.
     * @param search the value of the alert of a push notification
     * @param page The number of the page.
     * @param pageSize the number of elements in the result.
     *
     * @return list of push message info objects
     */
    List<FlatPushMessageInformation> findAllForPushApplicationByParams(String pushApplicationId, String search, boolean ascending, Integer page, Integer pageSize);

    /**
     * Loads paged push message metadata objects for the given PushApplication, but offers a way to order (asc/desc) by date.
     *
     * @param pushApplicationId ID of the PushApplication
     * @param ascending boolean to define if ASC ordering (by date) or DESC ordering should be executed.
     * @param search the value of the alert of a push notification
     * @param page The number of the page.
     * @param pageSize the number of elements in the result.
     *
     * @return list of push message info objects
     */
    MessageMetrics findMessageMetricsForPushApplicationByParams(String pushApplicationId, String search, boolean ascending, Integer page, Integer pageSize);

    /**
     * Loads paged push message metadata objects for the given PushApplication, but offers a way to order (asc/desc) by date.
     *
     * @param pushApplicationId ID of the PushApplication
     * @param ascending boolean to define if ASC ordering (by date) or DESC ordering should be executed.
     * @param search the value of the alert of a push notification
     * @param page The number of the page.
     * @param pageSize the number of elements in the result.
     *
     * @return list of push message info objects
     */
    PageResult<FlatPushMessageInformation, MessageMetrics> findAllForPushApplication(String pushApplicationId, String search, boolean ascending, Integer page, Integer pageSize);

    /**
     * Filters those variantIDs where the variant shows errors/issues for previous message sends
     *
     * @param loginName the username
     *
     * @return list of variant ids
     */
    List<String> findVariantIDsWithWarnings(String loginName);

    /**
     * Filters the three most recent FlatPushMessageInformation objects
     *
     * @param loginName the username
     * @param maxResults number of max items in the returned list
     *
     * @return list of push message info objects
     */
    List<FlatPushMessageInformation> findLatestActivity(String loginName, int maxResults);

    /**
     * Delete all Push Message Information entries that are older than the given date
     *
     * @param oldest the point in time to go back to
     */
    void deletePushInformationOlderThan(Date oldest);

    //Admin queries
    List<String> findVariantIDsWithWarnings();
    List<FlatPushMessageInformation> findLatestActivity(int maxResults);
    long getNumberOfPushMessagesForApplications();
}
