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

import javax.servlet.http.HttpServletRequest;
import java.util.regex.Pattern;

/**
 * Helper for various tasks for working with {@link javax.servlet.http.HttpServletRequest} objects.
 */
public final class HttpRequestUtil {

    private HttpRequestUtil() {
        // no-op
    }

    // from JSR 303
    private static final Pattern IP_ADDR_PATTERN = Pattern.compile("^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$");

    /**
     * Extracts the IP address from the given {@link javax.servlet.http.HttpServletRequest}.
     *
     * @param request to inspect
     *
     * @return the IP address from the given request
     */
    public static String extractIPAddress(final HttpServletRequest request) {

        String ip = request.getHeader("x-forwarded-for");
        if (isIPAdressValid(ip)) {
            return ip;
        }
        ip = request.getHeader("Proxy-Client-IP");
        if (isIPAdressValid(ip)) {
            return ip;
        }
        ip = request.getHeader("WL-Proxy-Client-IP");
        if (isIPAdressValid(ip)) {
            return ip;
        }
        return request.getRemoteAddr();
    }

    /**
     * Reads the "aerogear-sender" header to check if an AeroGear Sender client was used. If the header value is NULL
     * the value of the standard "user-agent" header is returned
     *
     * @param request to inspect
     *
     * @return value of header
     */
    public static String extractAeroGearSenderInformation(final HttpServletRequest request) {
        String client = request.getHeader("aerogear-sender");
        if (hasValue(client)) {
            return client;
        }
        // if there was no usage of our custom header, we simply return the user-agent value
        return request.getHeader("user-agent");
    }

    /**
     * Simple validation, using java.net.InetAddress.getByName().
     *
     * @param ip the IP address string to check
     *
     * @return true for a valid IP address
     */
    private static boolean isIPAdressValid(final String ip){

        // InetAddress.getByName() validates 'null' as a valid IP (localhost).
        // we do not want that
        if (hasValue(ip)) {
            return IP_ADDR_PATTERN.matcher(ip).matches();
        }
        return false;
    }

    private static boolean hasValue(final String value) {
        return value != null && !value.isEmpty();
    }
}
