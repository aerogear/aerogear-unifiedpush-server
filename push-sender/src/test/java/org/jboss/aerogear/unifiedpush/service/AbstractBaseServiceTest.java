/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.unifiedpush.service;

import org.jboss.aerogear.unifiedpush.dao.InstallationDao;
import org.jboss.aerogear.unifiedpush.dao.PushApplicationDao;
import org.jboss.aerogear.unifiedpush.jpa.dao.impl.JPACategoryDao;
import org.jboss.aerogear.unifiedpush.jpa.dao.impl.JPAInstallationDao;
import org.jboss.aerogear.unifiedpush.jpa.dao.impl.JPAVariantDao;
import org.jboss.aerogear.unifiedpush.service.annotations.LoggedIn;
import org.jboss.aerogear.unifiedpush.service.impl.PushSearchByDeveloperServiceImpl;
import org.jboss.aerogear.unifiedpush.service.impl.SearchManager;
import org.jboss.aerogear.unifiedpush.test.archive.UnifiedPushArchive;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Before;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.representations.AccessToken;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.NotSupportedException;
import javax.transaction.SystemException;

import static org.mockito.Mockito.when;

public abstract class AbstractBaseServiceTest {

    @Inject
    protected EntityManager entityManager;


    @Mock
    protected HttpServletRequest httpServletRequest;

    @Mock
    protected KeycloakSecurityContext context;

    @Mock
    protected KeycloakPrincipal keycloakPrincipal;

    @Inject
    protected SearchManager searchManager;

    @Inject
    protected PushApplicationService pushApplicationService;


    @Inject
    protected PushSearchByDeveloperServiceImpl searchApplicationService;

    // ===================== JUnit hooks =====================

    /**
     * Basic setup stuff, needed for all the UPS related service classes
     */
    @Before
    public void setUp() throws SystemException, NotSupportedException {
        // Keycloak test environment
        AccessToken token = new AccessToken();
        MockitoAnnotations.initMocks(this);
        //The current developer will always be the admin in this testing scenario
        token.setPreferredUsername("admin");
        when(context.getToken()).thenReturn(token);
        when(keycloakPrincipal.getKeycloakSecurityContext()).thenReturn(context);
        when(httpServletRequest.getUserPrincipal()).thenReturn(keycloakPrincipal);

        // glue it to serach mgr
        searchManager.setHttpServletRequest(httpServletRequest);
        entityManager.getTransaction().begin();

        // more to setup ?
        specificSetup();
    }

    @After
    public void tearDown() {

        entityManager.getTransaction().commit();
    }



    /**
     * Enforced to override to make sure test-case specific
     * setup is done inside here!
     */
    protected abstract void specificSetup();




}
