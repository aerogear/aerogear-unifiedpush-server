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
package org.jboss.aerogear.unifiedpush.rest.util;

import net.iharder.Base64;

import javax.servlet.http.HttpServletRequest;

import java.io.IOException;


public final class HttpBasicHelper {

    private HttpBasicHelper() {
    }

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
            String token = extractBasic(authorizationHeader);
            int delimiter = token.indexOf(':');

            if (delimiter != -1) {
                username = token.substring(0, delimiter);
                password = token.substring(delimiter + 1);
            }
        }
        return new String[] { username, password };
    }
    
    public static String extractBasic(String str) {
    	String base64Token = str.substring(6);
        return decodeBase64(base64Token);
    }
    
    public static String decodeBase64(String str) {
        try {
            return new String(Base64.decode(str));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
