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

import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.LockModeType;
import javax.persistence.TypedQuery;

import org.hibernate.Session;
import org.jboss.aerogear.unifiedpush.dao.GenericBaseDao;

public abstract class JPABaseDao<T, K> implements GenericBaseDao<T, K> {

    @Inject
    protected EntityManager entityManager;

    /**
     * Hook to manually inject an EntityManager.
     *
     * @param entityManager the EntityManager for this DAO class
     */
    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }


    protected TypedQuery<T> createQuery(String jpql) {
        return entityManager.createQuery(jpql, getType());
    }

    protected Query createUntypedQuery(String jpql) {
        return entityManager.createQuery(jpql);
    }

    protected <O> TypedQuery<O> createQuery(String jpql, Class<O> type) {
        return entityManager.createQuery(jpql, type);
    }

    protected org.hibernate.Query createHibernateQuery(String hql) {
        Session session = (Session) entityManager.getDelegate();
        return session.createQuery(hql);
    }

    //because you can't do T.class
    public abstract Class<T> getType();

    @Override
    public T find(K id) {
        return entityManager.find(getType(), id);
    }

    @Override
    public void create(T entity) {
        entityManager.persist(entity);
    }

    @Override
    public void update(T entity) {
        entityManager.merge(entity);
        entityManager.flush();
    }

    /**
     * Pessimistic write lock on entity
     */
    @Override
    public void lock(T entity) {
        entityManager.lock(entity, LockModeType.PESSIMISTIC_WRITE);
    }

    @Override
    public void delete(T entity) {
        if (entity != null) {
            // making sure the entity in question,
            // is really part of this transaction
            if (! entityManager.contains(entity)) {
                final Object entityId = entityManager.getEntityManagerFactory().getPersistenceUnitUtil().getIdentifier(entity);
                entity = entityManager.getReference(getType(), entityId);
            }

            entityManager.remove(entity);
        }
    }

    /**
     * Write pending objects to the database and
     * clear session-scoped cache
     */
    @Override
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
