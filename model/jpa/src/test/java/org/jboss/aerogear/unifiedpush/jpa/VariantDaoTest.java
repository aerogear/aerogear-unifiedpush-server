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
import org.jboss.aerogear.unifiedpush.api.Installation;
import org.jboss.aerogear.unifiedpush.api.Variant;
import org.jboss.aerogear.unifiedpush.dao.VariantDao;
import org.jboss.aerogear.unifiedpush.jpa.dao.impl.JPAInstallationDao;
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
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(Arquillian.class)
public class VariantDaoTest {

    @Inject
    private EntityManager entityManager;
    @Inject
    private VariantDao variantDao;
    private JPAInstallationDao installationDao;

    @Deployment
    public static JavaArchive createDeployment() {
        return DaoDeployment.createDeployment();
    }

    @Rule
    public EmbeddedDbTesterRule testDb = new EmbeddedDbTesterRule("Variant.xml");

    @Before
    public void setUp() {
        entityManager.getTransaction().begin();
    }

    @After
    public void tearDown() {
        entityManager.getTransaction().rollback();
    }

    @Test
    public void findVariantByIdForDeveloper() {
        assertThat(variantDao.findByVariantID("1")).isNotNull();
        assertThat(variantDao.findByVariantID(null)).isNull();
    }

    @Test
    public void findVariantIDsForDeveloper() {
        assertThat(variantDao.findVariantIDsForDeveloper("admin")).isNotNull();
        assertThat(variantDao.findVariantIDsForDeveloper("admin")).containsOnly("1");
    }

    @Test
    public void findVariantsByIDs() {

        //two valid and two invalid variantIDs
        final List<String> variantIDs = Arrays.asList("1", "2", "foo", "bar");

        final List<Variant> variants = variantDao.findAllVariantsByIDs(variantIDs);

        assertThat(variants).hasSize(2);
        assertThat(variants).extracting("name").contains("Android Variant", "Something more Android");
    }

    @Test
    public void updateVariant() {
        AndroidVariant queriedVariant = (AndroidVariant) variantDao.findByVariantID("1");
        final String primaryKey = queriedVariant.getId();
        assertThat(queriedVariant).isNotNull();
        assertThat(queriedVariant.getGoogleKey()).isEqualTo("KEY");

        queriedVariant.setGoogleKey("NEW_KEY");
        variantDao.update(queriedVariant);

        queriedVariant = (AndroidVariant) variantDao.findByVariantID("1");
        assertThat(queriedVariant).isNotNull();
        assertThat(queriedVariant.getGoogleKey()).isEqualTo("NEW_KEY");
        assertThat(queriedVariant.getId()).isEqualTo(primaryKey);
    }

    @Test
    public void updateAndDeleteVariant() {
        AndroidVariant queriedVariant = (AndroidVariant) variantDao.findByVariantID("1");
        final String primaryKey = queriedVariant.getId();
        assertThat(queriedVariant).isNotNull();
        assertThat(queriedVariant.getGoogleKey()).isEqualTo("KEY");

        queriedVariant.setGoogleKey("NEW_KEY");
        variantDao.update(queriedVariant);

        queriedVariant = (AndroidVariant) variantDao.findByVariantID("1");
        assertThat(queriedVariant).isNotNull();
        assertThat(queriedVariant.getGoogleKey()).isEqualTo("NEW_KEY");
        assertThat(queriedVariant.getId()).isEqualTo(primaryKey);

        variantDao.delete(queriedVariant);
        assertThat(variantDao.findByVariantID("1")).isNull();
    }

    @Test
    public void lookupNonExistingVariant() {
        AndroidVariant variant = (AndroidVariant) variantDao.findByVariantID("NOT-IN-DATABASE");
        assertThat(variant).isNull();
    }

    @Test
    public void variantIDUnmodifiedAfterUpdate() {
        AndroidVariant queriedVariant = (AndroidVariant) variantDao.findByVariantID("1");
        final String primaryKey = queriedVariant.getId();
        assertThat(queriedVariant.getVariantID()).isEqualTo("1");
        assertThat(queriedVariant).isNotNull();

        queriedVariant.setGoogleKey("NEW_KEY");
        variantDao.update(queriedVariant);

        queriedVariant = (AndroidVariant) variantDao.findByVariantID("1");
        assertThat(queriedVariant).isNotNull();
        assertThat(queriedVariant.getVariantID()).isEqualTo("1");
        assertThat(queriedVariant.getId()).isEqualTo(primaryKey);
    }

    @Test
    public void primaryKeyUnmodifiedAfterUpdate() {
        final String id  = "1";

        AndroidVariant variant = (AndroidVariant) variantDao.find(id);
        assertThat(variant.getId()).isEqualTo(id);

        variant.setGoogleKey("NEW_KEY");
        variantDao.update(variant);

        entityManager.flush();
        entityManager.clear();

        variant = (AndroidVariant) variantDao.find(id);

        assertThat(variant.getGoogleKey()).isEqualTo("NEW_KEY");

        assertThat(variant.getId()).isEqualTo(id);
    }

    @Test
    public void deleteVariantIncludingInstallations() {
        AndroidVariant queriedVariant = (AndroidVariant) variantDao.findByVariantID("1");

        variantDao.delete(queriedVariant);
        entityManager.flush();
        entityManager.clear();
        assertThat(variantDao.findByVariantID("1")).isNull();

        // Installation should be gone...
        assertThat(entityManager.find(Installation.class, "1")).isNull();
    }


    @Test
    public void shouldDetectThatVariantIdNotExists() {
        //given
        String nonExistentVariantId = "321-variantId";

        //when
        final boolean exists = variantDao.existsVariantIDForDeveloper(nonExistentVariantId, "admin");

        assertThat(exists).isEqualTo(false);
    }

    @Test
    public void shouldDetectThatVariantIdExists() {
        assertThat(variantDao.existsVariantIDForDeveloper("1", "admin")).isEqualTo(true);
        assertThat(variantDao.existsVariantIDForDeveloper("1", "foo")).isEqualTo(false);
    }

    @Test
    public void shouldDetectThatVariantIdExistsForAdmin() {
        assertThat(variantDao.existsVariantIDForAdmin("1")).isEqualTo(true);
        assertThat(variantDao.existsVariantIDForDeveloper("2", "foo")).isEqualTo(true);
    }
}
