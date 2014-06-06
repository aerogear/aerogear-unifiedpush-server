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

import org.jboss.aerogear.unifiedpush.api.PushMessageInformation;
import org.jboss.aerogear.unifiedpush.api.VariantMetricInformation;

import java.util.List;
import java.util.Set;

public interface PushMessageInformationDao extends GenericBaseDao<PushMessageInformation, String>  {

    /**
     * Does a count for all the push message that have been submitted for the given PushApplication.
     */
    long getNumberOfPushMessagesForApplications(List<String> pushApplicationIds);

    /**
     * Loads all push message metadata objects for the given PushApplication, but offers a way to order (asc/desc) by date.
     */
    List<PushMessageInformation> findAllForPushApplication(String pushApplicationId, boolean ascending);

    /**
     * Loads all push message metadata objects for the given Variant, but offers a way to order (asc/desc) by date.
     */
    List<PushMessageInformation> findAllForVariant(String id, boolean ascending);

    /**
     * Filters those variantIDs where the variant shows errors/issues for previous message sends
     */
    List<String> findVariantIDsWithWarnings(List<String> allVariantIDs);

    /**
     * Filters the top three Variants that did receive the most messages
     */
    List<VariantMetricInformation> findTopThreeBusyVariants(Set<String> allVariantIDs);

}
