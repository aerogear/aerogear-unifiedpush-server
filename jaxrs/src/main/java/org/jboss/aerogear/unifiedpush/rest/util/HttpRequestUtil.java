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

import org.keycloak.KeycloakPrincipal;
import org.keycloak.KeycloakSecurityContext;

import javax.servlet.http.HttpServletRequest;

/**
 * Helper for various tasks for working with {@link javax.servlet.http.HttpServletRequest} objects.
 */
public final class HttpRequestUtil {

    private HttpRequestUtil() {
        // no-op
    }

    /**
     * Returns FALSE when sorting query value matches 'desc', otherwise it returns TRUE.
     */
    public static Boolean extractSortingQueryParamValue(String sorting) {

        if (sorting != null && sorting.equalsIgnoreCase("desc")) {
            return Boolean.FALSE;
        } else {
            return Boolean.TRUE;
        }
    }

    /**
     * Extracts the IP address from the given {@link javax.servlet.http.HttpServletRequest}.
     */
    public static String extractIPAddress(final HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (hasValue(ip)) {
            return ip;
        }
        ip = request.getHeader("Proxy-Client-IP");
        if (hasValue(ip)) {
            return ip;
        }
        ip = request.getHeader("WL-Proxy-Client-IP");
        if (hasValue(ip)) {
            return ip;
        }
        return request.getRemoteAddr();
    }

    /**
     * Reads the "aerogear-sender" header to check if an AeroGear Sender client was used. If the header value is NULL
     * the value of the standard "user-agent" header is returned
     */
    public static String extractAeroGearSenderInformation(final HttpServletRequest request) {
        String client = request.getHeader("aerogear-sender");
        if (hasValue(client)) {
            return client;
        }
        // if there was no usage of our custom header, we simply return the user-agent value
        return request.getHeader("user-agent");
    }

    private static boolean hasValue(String value) {
        return value != null && !value.isEmpty() && !"unknown".equalsIgnoreCase(value);
    }
}
