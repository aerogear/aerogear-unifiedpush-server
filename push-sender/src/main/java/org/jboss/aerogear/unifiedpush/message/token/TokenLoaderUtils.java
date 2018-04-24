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
package org.jboss.aerogear.unifiedpush.message.token;

import com.google.android.gcm.server.Constants;
import org.jboss.aerogear.unifiedpush.message.Criteria;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

public final class TokenLoaderUtils {

    private TokenLoaderUtils () {
        // no-op
    }

    /**
     * Extracts FCM topic names out of a given Criteria object (e.g. /topics/nameOfcategory).
     * If the Criteria is empty, the given variant ID will be returned as topic name (/topics/variantID)
     *
     * @param criteria the push message filter criteria
     * @param variantID variant ID, used as global fallback FCM topic name
     *
     * @return FCM topic names for the given push message criteria filter.
     */
    public static Set<String> extractFCMTopics(final Criteria criteria, final String variantID) {
        final Set<String> topics = new TreeSet<>();

        if (isEmptyCriteria(criteria)) {
            // use the variant 'convenience' topic
            topics.add(Constants.TOPIC_PREFIX + variantID);

        } else if (isCategoryOnlyCriteria(criteria)) {
            // use the given categories
            topics.addAll(criteria.getCategories().stream()
                    .map(category -> Constants.TOPIC_PREFIX + category)
                    .collect(Collectors.toList()));
        }
        return topics;
    }

    /**
     * Helper method to check if only categories are applied. Useful in FCM land, where we use topics
     */
    public static boolean isCategoryOnlyCriteria(final Criteria criteria) {

        return  isEmpty(criteria.getAliases()) &&      // we are not subscribing to alias topic (yet)
                isEmpty(criteria.getDeviceTypes()) &&  // we are not subscribing to device type topic (yet)
                !isEmpty(criteria.getCategories());    // BUT! categories are mapped to topics
    }

    /**
     * Helper method to check if all criteria are empty. Useful in FCM land, where we use topics.
     */
    public static boolean isEmptyCriteria(final Criteria criteria) {

        return  isEmpty(criteria.getAliases()) &&
                isEmpty(criteria.getDeviceTypes()) &&
                isEmpty(criteria.getCategories());
    }

    /**
     * Helper method to check if we should do FCM topic request.
     */
    public static boolean isFCMTopicRequest(final Criteria criteria) {
        return isEmptyCriteria(criteria) || isCategoryOnlyCriteria(criteria);
    }

    /**
     * Checks if the list is empty, and not null
     */
    private static boolean isEmpty(List list) {
        return list == null || list.isEmpty();
    }
}
