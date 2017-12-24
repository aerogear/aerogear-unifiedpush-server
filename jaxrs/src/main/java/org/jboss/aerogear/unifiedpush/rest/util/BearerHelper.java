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

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.jboss.aerogear.unifiedpush.api.Variant;
import org.jboss.aerogear.unifiedpush.service.GenericVariantService;
import org.jboss.aerogear.unifiedpush.service.impl.spring.KeycloakServiceImpl;
import org.keycloak.jose.jws.JWSInput;
import org.keycloak.jose.jws.JWSInputException;
import org.keycloak.representations.AccessToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;

public final class BearerHelper {
	private static final Logger logger = LoggerFactory.getLogger(KeycloakServiceImpl.class);

	private static final String BEARER_SCHEME = "Bearer";

	private BearerHelper() {
	}

	public static Variant extractVariantFromBearerHeader(GenericVariantService genericVariantService,
			HttpServletRequest request) {
		String clientId = extractClientId(request);
		if (StringUtils.isNotBlank(clientId)) {
			return genericVariantService.findVariantByKeycloakClientID(clientId);
		}

		return null;
	}

	public static String extractClientId(HttpServletRequest request) {
		String clientId = null;

		AccessToken token = getTokenDataFromBearer(request).orNull();
		if (token != null) {
			clientId = token.getIssuedFor();
		}

		return clientId;
	}

	public static Optional<AccessToken> getTokenDataFromBearer(HttpServletRequest request) {

		String tokenString = getBarearToken(request).orNull();

		if (tokenString != null) {
			try {
				JWSInput input = new JWSInput(tokenString);
				return Optional.of(input.readJsonContent(AccessToken.class));
			} catch (JWSInputException e) {
				logger.debug("could not parse token: ", e);
			}
		}

		return Optional.absent();
	}

	// Barear authentication allowed only using keycloack context
	public static Optional<String> getBarearToken(HttpServletRequest request) {
		Enumeration<String> authHeaders = request.getHeaders("Authorization");
		if (authHeaders == null || !authHeaders.hasMoreElements()) {
			return Optional.absent();
		}

		String tokenString = null;
		while (authHeaders.hasMoreElements()) {
			String[] split = authHeaders.nextElement().trim().split("\\s+");
			if (split == null || split.length != 2)
				continue;
			if (!split[0].equalsIgnoreCase(BEARER_SCHEME))
				continue;
			tokenString = split[1];
		}

		if (tokenString == null) {
			return Optional.absent();
		}
		return Optional.of(tokenString);
	}

	// Barear authentication request
	public static boolean isBearerExists(HttpServletRequest request) {
		return BearerHelper.getBarearToken(request).isPresent();
	}
}
