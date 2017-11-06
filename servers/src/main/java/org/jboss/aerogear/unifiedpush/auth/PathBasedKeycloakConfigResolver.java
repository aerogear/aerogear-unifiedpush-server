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

import java.io.InputStream;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.jboss.aerogear.unifiedpush.rest.RestWebApplication;
import org.jboss.logging.Logger;
import org.keycloak.adapters.KeycloakConfigResolver;
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.spi.HttpFacade.Request;
import org.keycloak.common.util.KeycloakUriBuilder;
import org.keycloak.constants.ServiceUrlConstants;

@WebListener
public class PathBasedKeycloakConfigResolver implements KeycloakConfigResolver, ServletContextListener {
	private static final Logger log = Logger.getLogger(PathBasedKeycloakConfigResolver.class);

	private static final Map<String, CustomKeycloakDeployment> cache = new ConcurrentHashMap<String, CustomKeycloakDeployment>();
	private static ServletContext context;

	public void contextInitialized(ServletContextEvent sce) {
		context = sce.getServletContext();
	}

	public void contextDestroyed(ServletContextEvent sce) {
		context = null;
	}

	@Override
	public KeycloakDeployment resolve(Request request) {
		String path = request.getURI();

		String realm = "keycloak";
		int isWebAppIndex = path.indexOf(RestWebApplication.UPSI_BASE_CONTEXT);
		if (isWebAppIndex != -1) {
			realm = "upsi";
		}

		CustomKeycloakDeployment deployment = cache.get(realm);
		if (null == deployment) {
			InputStream is = context.getResourceAsStream("/WEB-INF/" + realm + ".json");

			if (is == null) {
				throw new IllegalStateException("Not able to find the file /" + realm + ".json");
			}

			deployment = CustomKeycloakDeploymentBuilder.build(is);

			String baseUrl = getBaseBuilder(deployment, request, deployment.getAuthServerBaseUrl()).build().toString();
			KeycloakUriBuilder serverBuilder = KeycloakUriBuilder.fromUri(baseUrl);
			resolveUrls(deployment, serverBuilder);

			cache.put(realm, deployment);
		}

		return deployment;
	}

	protected KeycloakUriBuilder getBaseBuilder(CustomKeycloakDeployment deployment, Request requestFacade,
			String base) {
		KeycloakUriBuilder builder = KeycloakUriBuilder.fromUri(base);
		URI request = URI.create(requestFacade.getURI());
		String scheme = request.getScheme();
		if (deployment.getSslRequired().isRequired(requestFacade.getRemoteAddr())) {
			scheme = "https";
			if (!request.getScheme().equals(scheme) && request.getPort() != -1) {
				log.error("request scheme: " + request.getScheme() + " ssl required");
				throw new RuntimeException("Can't resolve relative url from adapter config.");
			}
		}
		builder.scheme(scheme);
		builder.host(request.getHost());
		if (request.getPort() != -1) {
			builder.port(request.getPort());
		}
		return builder;
	}

	/**
	 * @param authUrlBuilder
	 *            absolute URI
	 */
	protected void resolveUrls(CustomKeycloakDeployment deployment, KeycloakUriBuilder authUrlBuilder) {
		if (log.isDebugEnabled()) {
			log.debug("resolveUrls");
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
}
