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

import org.apache.openejb.testing.Module;
import org.jboss.aerogear.unifiedpush.service.impl.SearchManager;

import javax.annotation.PreDestroy;
import javax.ejb.Stateful;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.Produces;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.io.Serializable;

public abstract class AbstractBaseServiceTest {

    @Module
    public Class<?>[] app() throws Exception {
        return new Class<?>[] { EntityManagerProducer.class, SearchManager.class};
    }

    // test-ware: EM producer:

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
