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
package org.jboss.aerogear.unifiedpush.cdi;

import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;

import org.picketlink.annotations.PicketLink;

/**
 * CDI Utility class, which contains various producer / factory methods.
 */
public class Factory {

    @Produces
    @PicketLink
    @PersistenceContext(unitName = "picketlink-default")
    private EntityManager picketLinkEntityManager;

    @PersistenceContext(unitName = "unifiedpush-default", type = PersistenceContextType.EXTENDED)
    private EntityManager entityManager;

    /**
     * Creates the {@code EntityManager} object for the UnifiedPush server.
     * 
     * @return the {@code EntityManager} object for the UnifiedPush server.
     */
    @Produces
    public EntityManager createEntityManager() {
        return entityManager;
    }

    /**
     * A disposer method performs cleanup for the {@code EntityManager} object. 
     * 
     * @param entityManager the provided {@code EntityManager} for which we perform the cleanup.
     */
    public void dispose(@Disposes EntityManager entityManager) {
        if (entityManager != null) {
            entityManager.close();
        }
    }
}
