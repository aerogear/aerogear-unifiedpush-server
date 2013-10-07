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

package org.jboss.aerogear.unifiedpush.service.sender.message;

import java.util.Map;

/**
 * Base interface for the different Payload objects.
 *
 * For details have a look at the <a href="http://aerogear.org/docs/specs/aerogear-push-messages/">Message Format Specification</a>.
 */
public interface UnifiedPushMessage {

    /**
     * Returns the value of the 'alert' key from the submitted payload.
     * This key is recognized in native iOS, without any API invocation and
     * on AeroGear's GCM offerings.
     * 
     * Android users that are not using AGDROID can read the value as well,
     * but need to call specific APIs to show the 'alert' value.
     */
    String getAlert();

    /**
     * Returns the value of the 'sound' key from the submitted payload.
     * This key is recognized in native iOS, without any API invocation.
     * 
     * Android users can read the value as well, but need to call specific
     * APIs to play the referenced 'sound' file.
     */
    String getSound();

    /**
     * Returns the value of the 'badge' key from the submitted payload.
     * This key is recognized in native iOS, without any API invocation.
     * 
     * Android users can read the value as well, but need to call specific
     * APIs to show the 'badge number'.
     */
    int getBadge();

    /**
     * Returns a Map, representing any other key-value pairs that were send
     * to the RESTful Sender API.
     * 
     * This map usually contains application specific data, like:
     * <pre>
     *  "sport-news-channel15" : "San Francisco 49er won last game" 
     * </pre>
     */
    Map<String, Object> getData();
}
