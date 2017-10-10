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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import org.jboss.aerogear.unifiedpush.api.Category;
import org.jboss.aerogear.unifiedpush.dao.CategoryDao;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import net.jakubholy.dbunitexpress.EmbeddedDbTesterRule;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = { JPAConfig.class })
@Transactional
public class CategoryDaoTest {

    @Inject
    private CategoryDao categoryDao;

    @Rule
    public EmbeddedDbTesterRule testDb = new EmbeddedDbTesterRule("Categories.xml");

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
