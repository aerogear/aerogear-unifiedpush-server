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
import javax.servlet.http.HttpServletRequest;

import org.jboss.aerogear.unifiedpush.service.impl.PushSearchByDeveloperServiceImpl;
import org.jboss.aerogear.unifiedpush.service.impl.SearchManager;
import org.jboss.aerogear.unifiedpush.test.archive.UnifiedPushServiceArchive;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.representations.AccessToken;
import org.mockito.Mock;
import org.mockito.Mockito;

@RunWith(Arquillian.class)
public abstract class AbstractBaseServiceTest {

    @Mock
    protected HttpServletRequest httpServletRequest = Mockito.mock(HttpServletRequest.class);

    @Mock
    protected KeycloakSecurityContext context = Mockito.mock(KeycloakSecurityContext.class);

    @Mock
    protected KeycloakPrincipal keycloakPrincipal = Mockito.mock(KeycloakPrincipal.class);;

    @Inject
    protected SearchManager searchManager;

    @Inject
    protected PushApplicationService pushApplicationService;

    @Inject
    protected PushSearchByDeveloperServiceImpl searchApplicationService;

    @Deployment
    public static WebArchive archive() {
        return UnifiedPushServiceArchive.forTestClass(AbstractBaseServiceTest.class)
        		.addMavenDependencies("org.jboss.aerogear.unifiedpush:unifiedpush-model-jpa")
        		.addMavenDependencies("org.keycloak:keycloak-core")
        		.addPackages(true, Configuration.class.getPackage())
        		.addAsLibrary("org.jboss.aerogear.unifiedpush:unifiedpush-model-jpa", new String[]{"META-INF/persistence.xml"}, new String[] {"META-INF/test-persistence.xml"})
                .addAsWebInfResource("META-INF/test-ds.xml", "test-ds.xml")
                .addAsResource("cert/certificate.p12")
                .addAsResource("default.properties")
                .withMockito()
                .withAssertj()
                .withLang()
                .withHttpclient()
                .as(WebArchive.class);
    }
    
    /**
     * Basic setup stuff, needed for all the UPS related service classes
     */
    @Before
    public void setUp(){
        // Keycloak test environment
        AccessToken token = new AccessToken();
        //The current developer will always be the admin in this testing scenario
        token.setPreferredUsername("admin");
        when(context.getToken()).thenReturn(token);
        when(keycloakPrincipal.getKeycloakSecurityContext()).thenReturn(context);
        when(httpServletRequest.getUserPrincipal()).thenReturn(keycloakPrincipal);

        // glue it to serach mgr
        searchManager.setHttpServletRequest(httpServletRequest);

        // more to setup ?
        specificSetup();
    }

    /**
     * Enforced to override to make sure test-case specific
     * setup is done inside here!
     */
    protected abstract void specificSetup();


}
