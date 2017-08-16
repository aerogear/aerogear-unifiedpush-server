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

import java.util.UUID;

import javax.inject.Inject;
import javax.persistence.PersistenceException;

import org.jboss.aerogear.unifiedpush.api.AndroidVariant;
import org.jboss.aerogear.unifiedpush.dao.VariantDao;
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
public class PersistentObjectTest {

    @Inject
    private VariantDao variantDao;

    @Rule
    public EmbeddedDbTesterRule testDb = new EmbeddedDbTesterRule("AndroidVariant.xml");

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
