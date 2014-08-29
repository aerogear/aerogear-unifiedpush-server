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
package org.jboss.aerogear.unifiedpush.jpa.dao.impl;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;

public abstract class JPABaseDao {

    @Inject
    protected EntityManager entityManager;

    /**
     * Hook to manually inject an EntityManager.
     */
    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }


    protected Query createQuery(String jpql) {
        return entityManager.createQuery(jpql);
    }

    protected void persist(Object entity) {
        entityManager.joinTransaction();
        entityManager.persist(entity);
    }

    protected void merge(Object entity) {
        entityManager.joinTransaction();
        entityManager.merge(entity);

        entityManager.flush();
    }

    protected void remove(Object entity) {
        if (entity != null) {

            entityManager.joinTransaction();
            entityManager.remove(entity);
        }
    }

    /**
     * Write pending objects to the database and
     * clear session-scoped cache
     */
    public void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }
}
