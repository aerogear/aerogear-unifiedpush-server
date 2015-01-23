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

import org.jboss.aerogear.unifiedpush.dao.GenericBaseDao;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.List;

public abstract class JPABaseDao<T, K> implements GenericBaseDao<T, K> {

    @Inject
    protected EntityManager entityManager;

    /**
     * Hook to manually inject an EntityManager.
     */
    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }


    protected TypedQuery<T> createQuery(String jpql) {
        return entityManager.createQuery(jpql, getType());
    }

    protected <O> TypedQuery<O> createQuery(String jpql, Class<O> type) {
        return entityManager.createQuery(jpql, type);
    }

    //because you can't do T.class
    public abstract Class<T> getType();

    public T find(K id) {
        return entityManager.find(getType(), id);
    }

    public void create(T entity) {
        entityManager.persist(entity);
    }

    public void update(T entity) {
        entityManager.merge(entity);
        entityManager.flush();
    }

    public void delete(T entity) {
        if (entity != null) {
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

    protected T getSingleResultForQuery(TypedQuery<T> query) {
        List<T> result = query.getResultList();

        if (!result.isEmpty()) {
            return result.get(0);
        } else {
            return null;
        }
    }
}
