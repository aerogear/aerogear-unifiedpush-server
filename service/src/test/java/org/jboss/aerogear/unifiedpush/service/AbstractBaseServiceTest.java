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
package org.jboss.aerogear.unifiedpush.service;

import static org.mockito.Mockito.when;

import javax.inject.Inject;

import org.jboss.aerogear.unifiedpush.service.annotations.LoggedInUser;
import org.jboss.aerogear.unifiedpush.spring.ServiceConfig;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.representations.AccessToken;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = { ServiceConfig.class })
public abstract class AbstractBaseServiceTest {

	protected static final String DEFAULT_USER = "admin";
	@Mock
	protected KeycloakSecurityContext context = Mockito.mock(KeycloakSecurityContext.class);

	@Mock
	protected KeycloakPrincipal keycloakPrincipal = Mockito.mock(KeycloakPrincipal.class);

	@Inject
	protected PushApplicationService pushApplicationService;
	@Inject
	@Qualifier("PushSearchByDeveloperServiceImpl")
	protected PushSearchService searchApplicationService;

	/**
	 * Basic setup stuff, needed for all the UPS related service classes
	 */
	@Before
	public void setUp() {
		searchApplicationService.setLoginName(new LoggedInUser(DEFAULT_USER));

		// Keycloak test environment
		AccessToken token = new AccessToken();
		// The current developer will always be the admin in this testing
		// scenario
		token.setPreferredUsername(DEFAULT_USER);
		when(context.getToken()).thenReturn(token);
		when(keycloakPrincipal.getKeycloakSecurityContext()).thenReturn(context);
		// when(httpServletRequest.getUserPrincipal()).thenReturn(keycloakPrincipal);

		// more to setup ?
		specificSetup();
	}

	/**
	 * Enforced to override to make sure test-case specific setup is done inside
	 * here!
	 */
	protected abstract void specificSetup();

	protected void sleepSilently(long milliseconds) {
		// Wait to cassandra persistance.
		try {
			Thread.sleep(milliseconds);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
