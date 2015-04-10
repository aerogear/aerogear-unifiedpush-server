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

import net.jakubholy.dbunitexpress.EmbeddedDbTesterRule;
import org.jboss.aerogear.unifiedpush.api.AndroidVariant;
import org.jboss.aerogear.unifiedpush.jpa.dao.impl.JPAVariantDao;
import org.jboss.aerogear.unifiedpush.utils.DaoDeployment;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import java.util.UUID;

@RunWith(Arquillian.class)
public class PersistentObjectTest {

    @Inject
    private EntityManager entityManager;
    @Inject
    private JPAVariantDao variantDao;

    @Deployment
    public static JavaArchive createDeployment() {
        return DaoDeployment.createDeployment();
    }

    @Rule
    public EmbeddedDbTesterRule testDb = new EmbeddedDbTesterRule("AndroidVariant.xml");

    @Before
    public void setUp() {
        entityManager.getTransaction().begin();
    }

    @After
    public void tearDown() {
        entityManager.getTransaction().rollback();
    }

    @Test
    public void saveObject() {
        final AndroidVariant av = new AndroidVariant();
        av.setGoogleKey("KEY");
        av.setDeveloper("admin");

        variantDao.create(av);
    }

    @Test(expected = PersistenceException.class)
    public void updateId() {

        final AndroidVariant av = new AndroidVariant();
        av.setGoogleKey("KEY");
        av.setDeveloper("admin");

        variantDao.create(av);

        av.setId(UUID.randomUUID().toString());
        variantDao.update(av);
    }
}
