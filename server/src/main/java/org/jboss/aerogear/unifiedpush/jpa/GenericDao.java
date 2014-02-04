/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.unifiedpush.jpa;

import javax.persistence.Query;

public interface GenericDao<E, K> {

    /**
     * Creates {@link Query} object, based on given JPQL string.
     * 
     * @param the string that will be transformed into the {@link Query} object.
     * @return {@link Query} object, based on given string
     */
    Query createQuery(String jpql);

    /**
     * Finder to look up a raw type, based on it's primary key.
     *  
     * @param classType type of the JPA Entiry
     * @param id the primary ke
     * @return the entity or null if not found.
     */
    E find(Class<?> classType, K id);

    /**
     * Persists a given object
     * 
     * @param e the entity to be persisted
     * @return the persisted entity
     */
    E create(E e);

    /**
     * Updates a given entity.
     * 
     * @param e the entity to be updated
     * @return the updated entity
     */
    E update(E e);

    /**
     * Deletes a given entity.
     * 
     * @param e the entity to be deleted
     */
    void delete(E e);

    /**
     * Returns the JPA singleResult, based on a given {@link Query} object.
     * 
     * @param query identifying a query against the underlying datastore. 
     * @return the singleResult entity or null, if not found.
     */
    E getSingleResultForQuery(Query query);
}
