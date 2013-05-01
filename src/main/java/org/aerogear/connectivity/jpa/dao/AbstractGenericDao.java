/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors
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

package org.aerogear.connectivity.jpa.dao;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;

public class AbstractGenericDao<E, K> implements GenericDao<E, K> {

    @Inject protected EntityManager entityManager;

    // ---------------------- Transaction methods ----------------------
    public E create(E e) {
        entityManager.joinTransaction();
        entityManager.persist(e);
        return e; 
    }

    public E update(E e) {
        entityManager.joinTransaction();
        E ent = entityManager.merge(e);
        entityManager.flush();
        return ent;
        
    }

    public void delete(E e) {
        entityManager.joinTransaction();
        entityManager.remove(e);
    }

    // ---------------------- (generic) finder methods ----------------------
    public Query createQuery(String jpql) {
        return entityManager.createQuery(jpql);
    }
    
    @SuppressWarnings("unchecked")
    public E find(Class<?> classType, K id) {
        return (E) entityManager.find(classType, id);
    }
}
