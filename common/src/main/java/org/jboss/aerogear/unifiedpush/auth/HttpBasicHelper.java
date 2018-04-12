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
package org.jboss.aerogear.unifiedpush.auth;


import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.Base64;


public final class HttpBasicHelper {

    private static final String HTTP_BASIC_SCHEME = "Basic ";

    private HttpBasicHelper() {
    }

    private static boolean isBasic(final String authorizationHeader) {
        return authorizationHeader.startsWith(HTTP_BASIC_SCHEME);
    }

    private static String getAuthorizationHeader(final HttpServletRequest request) {
        return request.getHeader("Authorization");
    }

    public static String[] extractUsernameAndPasswordFromBasicHeader(final HttpServletRequest request) {
        String username = "";
        String password = "";
        final String authorizationHeader = getAuthorizationHeader(request);

        if (authorizationHeader != null && isBasic(authorizationHeader)) {
            final String base64Token = authorizationHeader.substring(HTTP_BASIC_SCHEME.length());
            final String token = new String(Base64.getDecoder().decode(base64Token), StandardCharsets.UTF_8);

            final int delimiter = token.indexOf(':');

            if (delimiter != -1) {
                username = token.substring(0, delimiter);
                password = token.substring(delimiter + 1);
            }
        }
        return new String[] { username, password };
    }
}
