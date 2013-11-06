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

import org.jboss.aerogear.unifiedpush.model.AndroidVariant;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class VariantDaoTest {

    private EntityManager entityManager;
    private VariantDaoImpl variantDao;


    @Before
    public void setUp() {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("UnifiedPush");
        entityManager = emf.createEntityManager();

        // start the shindig
        entityManager.getTransaction().begin();

        variantDao = new VariantDaoImpl();
        variantDao.setEntityManager(entityManager);

    }

    @After
    public void tearDown() {
        entityManager.getTransaction().commit();

        entityManager.close();
    }

    @Test
    public void findVariantByIdForDeveloper() {

        final AndroidVariant av = new AndroidVariant();
        av.setGoogleKey("KEY");
        av.setDeveloper("admin");
        final String uuid  = UUID.randomUUID().toString();
        av.setVariantID(uuid);

        variantDao.create(av);

        assertNotNull(variantDao.findByVariantIDForDeveloper(uuid, "admin"));
        assertNull(variantDao.findByVariantIDForDeveloper(null, "admin"));
        assertNull(variantDao.findByVariantIDForDeveloper(uuid, "mr x"));
    }

    @Test
    public void findVariantById() {

        final AndroidVariant av = new AndroidVariant();
        av.setGoogleKey("KEY");
        av.setDeveloper("admin");
        final String uuid  = UUID.randomUUID().toString();
        av.setVariantID(uuid);

        variantDao.create(av);

        assertNotNull(variantDao.findByVariantID(uuid));
        assertNull(variantDao.findByVariantID(null));
    }
}
