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

import java.util.Objects;

import javax.servlet.http.HttpServletRequest;

import org.keycloak.jose.jws.JWSInput;
import org.keycloak.jose.jws.JWSInputException;
import org.keycloak.representations.AccessToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jboss.aerogear.unifiedpush.service.GenericVariantService;
import org.jboss.aerogear.unifiedpush.service.impl.KeycloakServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.jboss.aerogear.unifiedpush.api.Variant;

public final class BearerHelper {
	private static final Logger logger = LoggerFactory.getLogger(KeycloakServiceImpl.class);

    private static final String BEARER_SCHEME = "Bearer ";

	private BearerHelper() {
	}

	private static boolean isBearer(String authorizationHeader) {
		return authorizationHeader.startsWith(BEARER_SCHEME);
	}

	private static String getAuthorizationHeader(HttpServletRequest request) {
		Objects.requireNonNull(request, "request may not be null");
		return request.getHeader("Authorization");
	}

	public static Variant extractVariantFromBearerHeader(GenericVariantService genericVariantService,
			HttpServletRequest request){
		String clientId = extractClientId(request);
		if (StringUtils.isNotBlank(clientId)){
			return genericVariantService.findVariantByKeycloakClientID(clientId);
		}

		return null;
	}

	public static String extractClientId(HttpServletRequest request) {
		String clientId = null;

        AccessToken token = getTokenDataFromBearer(request);
        if (token != null){
        	clientId = token.getIssuedFor();
        }

		return clientId;
	}

	public static AccessToken getTokenDataFromBearer(HttpServletRequest request){
		AccessToken accessToken = null;
		String authorizationHeader = getAuthorizationHeader(request);

		if (authorizationHeader != null && isBearer(authorizationHeader)) {
			String tokenString = extractBearerToken(authorizationHeader);

			try {
	        	JWSInput input = new JWSInput(tokenString);
	        	accessToken = input.readJsonContent(AccessToken.class);
	        } catch (JWSInputException e) {
	        	logger.debug("could not parse token: ", e);
	        }
		}

        return accessToken;
	}

	public static String extractBearerToken(String authorizationHeader) {
		return authorizationHeader.substring(BEARER_SCHEME.length());
	}
}
