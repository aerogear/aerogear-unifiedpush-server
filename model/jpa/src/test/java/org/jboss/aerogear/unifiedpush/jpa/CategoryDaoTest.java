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

import org.jboss.aerogear.unifiedpush.api.Category;
import org.jboss.aerogear.unifiedpush.jpa.dao.impl.JPACategoryDao;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.RollbackException;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class CategoryDaoTest {

    private EntityManager entityManager;
    private JPACategoryDao categoryDao;

    @Before
    public void setUp() {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("UnifiedPush");
        entityManager = emf.createEntityManager();

        // start the shindig
        entityManager.getTransaction().begin();

        createTestData(entityManager);
        categoryDao = new JPACategoryDao();
        categoryDao.setEntityManager(entityManager);
    }

    @After
    public void tearDown() {
        try {
            entityManager.getTransaction().commit();
        } catch (RollbackException e) {
            //ignore
        }

        entityManager.close();
    }

    private void createTestData(EntityManager entityManager) {
        Category cat1 = new Category("cat1");
        Category cat2 = new Category("cat2");
        Category cat3 = new Category("cat3");

        entityManager.persist(cat1);
        entityManager.persist(cat2);
        entityManager.persist(cat3);
    }

    @Test
    public void shouldFindCategoriesByName() {
        // given
        List<String> categoryNames = Arrays.asList("cat1", "cat2", "non-existing");

        // when
        final List<Category> names = categoryDao.findByNames(categoryNames);

        // then
        assertThat(names).hasSize(2).contains(new Category("cat1"), new Category("cat2"));
    }
}
