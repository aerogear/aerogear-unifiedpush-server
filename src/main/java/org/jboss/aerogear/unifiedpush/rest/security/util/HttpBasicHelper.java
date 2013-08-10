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
package org.jboss.aerogear.unifiedpush.rest.security.util;

import javax.servlet.http.HttpServletRequest;

import org.picketlink.common.util.Base64;

public final class HttpBasicHelper {

    private HttpBasicHelper() {
    };

    private static boolean isBasic(String authorizationHeader) {
        return authorizationHeader.startsWith("Basic ");
    }

    private static String getAuthorizationHeader(HttpServletRequest request) {
        return request.getHeader("Authorization");
    }

    public static String[] extractUsernameAndPasswordFromBasicHeader(HttpServletRequest request) {
        String username = "";
        String password = "";
        String authorizationHeader = getAuthorizationHeader(request);

        if (authorizationHeader != null && isBasic(authorizationHeader)) {
            String base64Token = authorizationHeader.substring(6);
            String token = new String(Base64.decode(base64Token));

            int delim = token.indexOf(":");

            if (delim != -1) {
                username = token.substring(0, delim);
                password = token.substring(delim + 1);
            }
        }
        return new String[] { username, password };
    }
}
