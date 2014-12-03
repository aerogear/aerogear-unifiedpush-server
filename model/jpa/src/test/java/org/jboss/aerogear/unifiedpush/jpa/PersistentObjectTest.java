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
package org.jboss.aerogear.unifiedpush.jpa;

import org.jboss.aerogear.unifiedpush.api.AndroidVariant;
import org.jboss.aerogear.unifiedpush.jpa.dao.impl.JPAVariantDao;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.PersistenceException;
import java.util.UUID;

public class PersistentObjectTest {

    private EntityManager entityManager;
    private JPAVariantDao variantDao;


    @Before
    public void setUp() {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("UnifiedPush");
        entityManager = emf.createEntityManager();

        // start the shindig
        entityManager.getTransaction().begin();

        variantDao = new JPAVariantDao();
        variantDao.setEntityManager(entityManager);
    }

    @Test
    public void saveObject() {
        final AndroidVariant av = new AndroidVariant();
        av.setGoogleKey("KEY");
        av.setDeveloper("admin");

        variantDao.create(av);
        entityManager.flush();
    }

    @Test(expected = PersistenceException.class)
    public void updateIdToNull() {

        final AndroidVariant av = new AndroidVariant();
        av.setGoogleKey("KEY");
        av.setDeveloper("admin");

        variantDao.create(av);

        av.setId(null);
        variantDao.update(av);

        entityManager.flush();
    }

    @Test(expected = PersistenceException.class)
    public void updateId() {

        final AndroidVariant av = new AndroidVariant();
        av.setGoogleKey("KEY");
        av.setDeveloper("admin");

        variantDao.create(av);

        av.setId(UUID.randomUUID().toString());
        variantDao.update(av);

        entityManager.flush();
    }
}
