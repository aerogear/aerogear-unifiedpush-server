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
package org.jboss.aerogear.unifiedpush.rest.util;

import java.net.MalformedURLException;
import java.net.URL;

public class CommonUtils {

    private CommonUtils() {
        // no-op
    }

    /**
     * Verify if the string sorting matches with asc or desc
     * Returns FALSE when sorting query value matches desc, otherwise it returns TRUE.
     *
     * @param sorting the sorting value from the http header
     * @return false for desc or true for as
     */
    public static Boolean isAscendingOrder(String sorting) {
        return "desc".equalsIgnoreCase(sorting) ? Boolean.FALSE : Boolean.TRUE;
    }

    public static String removeDefaultHttpPorts(final String uri) {
        URL url;
        try {
            url = new URL(uri);
            if (url.getPort() == url.getDefaultPort()) {

                String urlPort = ":" + url.getPort();

                return url.toExternalForm().replace(urlPort, "");
            }
        } catch (MalformedURLException e) {
            return null;
        }
        return url.toExternalForm();
    }
}
