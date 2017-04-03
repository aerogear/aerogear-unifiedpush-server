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


import java.nio.charset.StandardCharsets;
import java.util.Base64;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.HttpHeaders;

public final class HttpBasicHelper {

    private static final String HTTP_BASIC_SCHEME = "Basic ";

	private HttpBasicHelper() {
	}

	private static boolean isBasic(String authorizationHeader) {
        return authorizationHeader.startsWith(HTTP_BASIC_SCHEME);
	}

	public static String getAuthorizationHeader(HttpServletRequest request) {
		return request.getHeader(HttpHeaders.AUTHORIZATION);
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

	public static String extractBasic(String authorizationHeader) {
		final String base64Token = authorizationHeader.substring(HTTP_BASIC_SCHEME.length());
		return new String(Base64.getDecoder().decode(base64Token), StandardCharsets.UTF_8);
	}

	public static String decodeBase64(String str) {
		return new String(Base64.getDecoder().decode(str));
	}

	public static String encodeBase64(String str) {
		return new String(Base64.getEncoder().encode(str.getBytes()));
	}

	public static Invocation.Builder basic(Invocation.Builder request, String username, String password){
	    String auth = username + ":" + password;
	    String authHeader = "Basic " + new String(encodeBase64(auth));
		return request.header(HttpHeaders.AUTHORIZATION, authHeader);
	}
}
