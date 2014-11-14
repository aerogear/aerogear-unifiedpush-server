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

import org.apache.openejb.jee.Beans;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.mockito.MockitoInjector;
import org.apache.openejb.testing.MockInjector;
import org.apache.openejb.testing.Module;
import org.jboss.aerogear.unifiedpush.jpa.dao.impl.JPACategoryDao;
import org.jboss.aerogear.unifiedpush.jpa.dao.impl.JPAInstallationDao;
import org.jboss.aerogear.unifiedpush.jpa.dao.impl.JPAPushApplicationDao;
import org.jboss.aerogear.unifiedpush.jpa.dao.impl.JPAPushMessageInformationDao;
import org.jboss.aerogear.unifiedpush.jpa.dao.impl.JPAVariantDao;
import org.jboss.aerogear.unifiedpush.service.impl.ClientInstallationServiceImpl;
import org.jboss.aerogear.unifiedpush.service.impl.GenericVariantServiceImpl;
import org.jboss.aerogear.unifiedpush.service.impl.PushApplicationServiceImpl;
import org.jboss.aerogear.unifiedpush.service.impl.PushSearchByDeveloperServiceImpl;
import org.jboss.aerogear.unifiedpush.service.impl.PushSearchServiceImpl;
import org.jboss.aerogear.unifiedpush.service.impl.SearchManager;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.representations.AccessToken;
import org.mockito.Mock;

import javax.annotation.PreDestroy;
import javax.ejb.Stateful;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;

import static org.mockito.Mockito.when;

@RunWith(ApplicationComposer.class)
public abstract class AbstractBaseServiceTest {

    @Mock
    protected HttpServletRequest httpServletRequest;

    @Mock
    protected KeycloakSecurityContext context;

    @Mock
    protected KeycloakPrincipal keycloakPrincipal;

    @Inject
    protected SearchManager searchManager;

    @Inject
    protected PushApplicationServiceImpl pushApplicationService;

    @Inject
    protected PushSearchByDeveloperServiceImpl searchApplicationService;

    // ===================== JUnit hooks =====================

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

    // ===================== OpenEJB hooks and base methods =====================

    @MockInjector
    public Class<?> mockitoInjector() {
        return MockitoInjector.class;
    }

    @Module
    public Beans getBeans() {
        final Beans beans = new Beans();
        beans.addManagedClass(ClientInstallationServiceImpl.class);
        beans.addManagedClass(JPAPushMessageInformationDao.class);
        beans.addManagedClass(JPAInstallationDao.class);
        beans.addManagedClass(GenericVariantServiceImpl.class);
        beans.addManagedClass(JPAVariantDao.class);
        beans.addManagedClass(JPACategoryDao.class);
        beans.addManagedClass(PushSearchByDeveloperServiceImpl.class);
        beans.addManagedClass(PushApplicationServiceImpl.class);
        beans.addManagedClass(JPAPushApplicationDao.class);
        beans.addManagedClass(PushSearchServiceImpl.class);
        beans.addManagedClass(SearchManager.class);

        return beans;
    }

    @Module
    public Class<?>[] produceTestEntityManager() throws Exception {
        return new Class<?>[] { EntityManagerProducer.class};
    }

    /**
     * Static class to have OpenEJB produce/lookup a test EntityManager.
     */
    @SessionScoped
    @Stateful
    public static class EntityManagerProducer implements Serializable {

        {
            EntityManagerFactory emf = Persistence.createEntityManagerFactory("UnifiedPush");
            entityManager = emf.createEntityManager();
        }

        private static EntityManager entityManager;

        @Produces
        public EntityManager produceEm() {

            if (! entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().begin();
            }

            return entityManager;
        }

        @PreDestroy
        public void closeEntityManager() {
            if (entityManager.isOpen()) {
                entityManager.getTransaction().commit();
                entityManager.close();
            }
        }
    }
}
