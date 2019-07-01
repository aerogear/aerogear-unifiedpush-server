/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.unifiedpush.auth;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;
import org.jboss.aerogear.unifiedpush.rest.util.BearerHelper;
import org.jboss.aerogear.unifiedpush.rest.util.URLUtils;
import org.jboss.aerogear.unifiedpush.service.impl.spring.OAuth2Configuration;
import org.keycloak.adapters.KeycloakConfigResolver;
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.spi.HttpFacade.Request;
import org.keycloak.common.util.KeycloakUriBuilder;
import org.keycloak.constants.ServiceUrlConstants;
import org.keycloak.representations.AccessToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

public class PathBasedKeycloakConfigResolver implements KeycloakConfigResolver {
	private static final Logger logger = LoggerFactory.getLogger(PathBasedKeycloakConfigResolver.class);
	private static final String DEFAULT_REALM_FILE = "keycloak";

	// TODO - Convert to
	private static final Map<String, CustomKeycloakDeployment> cache = new ConcurrentHashMap<String, CustomKeycloakDeployment>();

	@Autowired
	private ApplicationContext applicationContext;

	@Override
	public KeycloakDeployment resolve(Request request) {
		String realm = DEFAULT_REALM_FILE;

		AccessToken accessToken = BearerHelper.getTokenDataFromBearer(request).orNull();
		if (accessToken != null) {
			String issuer = accessToken.getIssuer();

			if (StringUtils.isNoneEmpty(issuer)) {
				issuer = URLUtils.removeLastSlash(issuer);
				String jwtRealm = URLUtils.getLastPart(issuer);

				if (!OAuth2Configuration.DEFAULT_OAUTH2_UPS_REALM.equalsIgnoreCase(jwtRealm)) {
					realm = "upsi";
				}
			}
		}

		URI uri = URI.create(request.getURI());

		if (logger.isTraceEnabled())
			logger.trace("Identified Bearer request, using keycloak realm! URI: {}, realm: {}", uri.toString(), realm);

		String host = uri.getHost();
		CustomKeycloakDeployment deployment = cache.get(getCacheKey(realm, host));
		if (null == deployment) {
			InputStream is = null;

			try {
				is = applicationContext.getResource("/WEB-INF/" + realm + ".json").getInputStream();
			} catch (IOException e) {
				throw new IllegalStateException("Not able to find the file /" + realm + ".json");
			}

			if (is == null) {
				throw new IllegalStateException("Not able to find the file /" + realm + ".json");
			}

			deployment = CustomKeycloakDeploymentBuilder.build(is);

			String baseUrl = getBaseBuilder(deployment, request, deployment.getAuthServerBaseUrl()).build().toString();
			KeycloakUriBuilder serverBuilder = KeycloakUriBuilder.fromUri(baseUrl);
			resolveUrls(deployment, serverBuilder);

			cache.put(getCacheKey(realm, host), deployment);
		}

		return deployment;
	}

	protected KeycloakUriBuilder getBaseBuilder(CustomKeycloakDeployment deployment, Request requestFacade,
			String base) {
		KeycloakUriBuilder builder = KeycloakUriBuilder.fromUri(base);
		URI uri = URI.create(requestFacade.getURI());
		String scheme = uri.getScheme();
		if (deployment.getSslRequired().isRequired(requestFacade.getRemoteAddr())) {
			scheme = "https";
			if (!uri.getScheme().equals(scheme) && uri.getPort() != -1) {
				logger.error("request scheme: " + uri.getScheme() + " ssl required");
				throw new RuntimeException("Can't resolve relative url from adapter config.");
			}
		}
		builder.scheme(scheme);
		builder.host(uri.getHost());
		if (uri.getPort() != -1) {
			builder.port(uri.getPort());
		}
		return builder;
	}

	/**
	 * @param authUrlBuilder absolute URI
	 */
	protected void resolveUrls(CustomKeycloakDeployment deployment, KeycloakUriBuilder authUrlBuilder) {
		if (logger.isDebugEnabled()) {
			logger.debug("resolveUrls");
		}

		String login = authUrlBuilder.clone().path(ServiceUrlConstants.AUTH_PATH).build(deployment.getRealm())
				.toString();
		deployment.setAuthUrl(KeycloakUriBuilder.fromUri(login));
		deployment.setRealmInfoUrl(authUrlBuilder.clone().path(ServiceUrlConstants.REALM_INFO_PATH)
				.build(deployment.getRealm()).toString());

		deployment.setTokenUrl(
				authUrlBuilder.clone().path(ServiceUrlConstants.TOKEN_PATH).build(deployment.getRealm()).toString());
		deployment.setLogoutUrl(KeycloakUriBuilder.fromUri(authUrlBuilder.clone()
				.path(ServiceUrlConstants.TOKEN_SERVICE_LOGOUT_PATH).build(deployment.getRealm()).toString()));
		deployment.setAccountUrl(authUrlBuilder.clone().path(ServiceUrlConstants.ACCOUNT_SERVICE_PATH)
				.build(deployment.getRealm()).toString());
		deployment.setRegisterNodeUrl(
				authUrlBuilder.clone().path(ServiceUrlConstants.CLIENTS_MANAGEMENT_REGISTER_NODE_PATH)
						.build(deployment.getRealm()).toString());
		deployment.setUnregisterNodeUrl(
				authUrlBuilder.clone().path(ServiceUrlConstants.CLIENTS_MANAGEMENT_UNREGISTER_NODE_PATH)
						.build(deployment.getRealm()).toString());
	}

	private String getCacheKey(String realm, String host) {
		return new StringBuffer(realm).append("-").append(host).toString();
	}
}
