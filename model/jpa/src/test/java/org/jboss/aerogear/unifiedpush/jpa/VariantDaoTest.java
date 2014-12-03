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

import org.jboss.aerogear.unifiedpush.api.APNsVariant;
import org.jboss.aerogear.unifiedpush.api.AndroidVariant;
import org.jboss.aerogear.unifiedpush.api.Category;
import org.jboss.aerogear.unifiedpush.api.Installation;
import org.jboss.aerogear.unifiedpush.api.Variant;
import org.jboss.aerogear.unifiedpush.jpa.dao.impl.JPAInstallationDao;
import org.jboss.aerogear.unifiedpush.jpa.dao.impl.JPAVariantDao;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class VariantDaoTest {


    private EntityManager entityManager;
    private JPAVariantDao variantDao;
    private JPAInstallationDao installationDao;


    @Before
    public void setUp() {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("UnifiedPush");
        entityManager = emf.createEntityManager();

        // start the shindig
        entityManager.getTransaction().begin();

        variantDao = new JPAVariantDao();
        variantDao.setEntityManager(entityManager);
        installationDao = new JPAInstallationDao();
        installationDao.setEntityManager(entityManager);
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
        final String uuid  = av.getVariantID();

        variantDao.create(av);

        assertThat(variantDao.findByVariantID(uuid)).isNotNull();
        assertThat(variantDao.findByVariantID(null)).isNull();
    }

    @Test
    public void findVariantIDsForDeveloper() {

        final AndroidVariant av = new AndroidVariant();
        av.setGoogleKey("KEY");
        av.setDeveloper("admin");
        final String uuid  = av.getVariantID();

        variantDao.create(av);

        assertThat(variantDao.findVariantIDsForDeveloper("admin")).isNotNull();
        assertThat(variantDao.findVariantIDsForDeveloper("admin")).containsOnly(uuid);
    }

    @Test
    public void findVariantsByIDs() {

        final List<String> variantIDs = new ArrayList<String>(4);

        final AndroidVariant av1 = new AndroidVariant();
        av1.setName("Something Android");
        av1.setGoogleKey("KEY");
        av1.setDeveloper("admin");
        variantIDs.add(av1.getVariantID());
        variantDao.create(av1);
        final AndroidVariant av2 = new AndroidVariant();
        av2.setName("Something more Android");
        av2.setGoogleKey("KEY");
        av2.setDeveloper("admin");
        variantIDs.add(av2.getVariantID());
        variantDao.create(av2);

        // add some invalid IDs:
        variantIDs.add("foo");
        variantIDs.add("bar");

        final List<Variant> variants = variantDao.findAllVariantsByIDs(variantIDs);

        assertThat(variants).hasSize(2);
        assertThat(variants).extracting("name").contains("Something Android", "Something more Android");
    }

    @Test
    public void findVariantById() {

        final AndroidVariant av = new AndroidVariant();
        av.setGoogleKey("KEY");
        av.setDeveloper("admin");
        final String uuid  = av.getVariantID();

        variantDao.create(av);

        assertThat(variantDao.findByVariantID(uuid)).isNotNull();
        assertThat(variantDao.findByVariantID(null)).isNull();
    }

    @Test
    public void updateVariant() {

        final AndroidVariant av = new AndroidVariant();
        av.setGoogleKey("KEY");
        av.setDeveloper("admin");
        final String uuid  = av.getVariantID();

        variantDao.create(av);

        AndroidVariant queriedVariant = (AndroidVariant) variantDao.findByVariantID(uuid);
        final String primaryKey = queriedVariant.getId();
        assertThat(queriedVariant).isNotNull();
        assertThat(queriedVariant.getGoogleKey()).isEqualTo("KEY");

        queriedVariant.setGoogleKey("NEW_KEY");
        variantDao.update(queriedVariant);

        queriedVariant = (AndroidVariant) variantDao.findByVariantID(uuid);
        assertThat(queriedVariant).isNotNull();
        assertThat(queriedVariant.getGoogleKey()).isEqualTo("NEW_KEY");
        assertThat(queriedVariant.getId()).isEqualTo(primaryKey);
    }

    @Test
    public void updateAndDeleteVariant() {

        final AndroidVariant av = new AndroidVariant();
        av.setGoogleKey("KEY");
        av.setDeveloper("admin");
        final String uuid  = av.getVariantID();

        variantDao.create(av);

        AndroidVariant queriedVariant = (AndroidVariant) variantDao.findByVariantID(uuid);
        final String primaryKey = queriedVariant.getId();
        assertThat(queriedVariant).isNotNull();
        assertThat(queriedVariant.getGoogleKey()).isEqualTo("KEY");

        queriedVariant.setGoogleKey("NEW_KEY");
        variantDao.update(queriedVariant);

        queriedVariant = (AndroidVariant) variantDao.findByVariantID(uuid);
        assertThat(queriedVariant).isNotNull();
        assertThat(queriedVariant.getGoogleKey()).isEqualTo("NEW_KEY");
        assertThat(queriedVariant.getId()).isEqualTo(primaryKey);

        variantDao.delete(queriedVariant);
        assertThat(variantDao.findByVariantID(uuid)).isNull();
    }

    @Test
    public void lookupNonExistingVariant() {
        AndroidVariant variant = (AndroidVariant) variantDao.findByVariantID("NOT-IN-DATABASE");
        assertThat(variant).isNull();
    }

    @Test
    public void variantIDUnmodifiedAfterUpdate() {

        final AndroidVariant av = new AndroidVariant();
        av.setGoogleKey("KEY");
        av.setDeveloper("admin");
        final String uuid  = av.getVariantID();

        variantDao.create(av);

        AndroidVariant queriedVariant = (AndroidVariant) variantDao.findByVariantID(uuid);
        final String primaryKey = queriedVariant.getId();
        assertThat(queriedVariant.getVariantID()).isEqualTo(uuid);
        assertThat(queriedVariant).isNotNull();

        queriedVariant.setGoogleKey("NEW_KEY");
        variantDao.update(queriedVariant);

        queriedVariant = (AndroidVariant) variantDao.findByVariantID(uuid);
        assertThat(queriedVariant).isNotNull();
        assertThat(queriedVariant.getVariantID()).isEqualTo(uuid);
        assertThat(queriedVariant.getId()).isEqualTo(primaryKey);
    }

    @Test
    public void primaryKeyUnmodifiedAfterUpdate() {
        AndroidVariant av = new AndroidVariant();
        av.setGoogleKey("KEY");
        av.setDeveloper("admin");
        final String id  = av.getId();

        variantDao.create(av);

        // flush to be sure that it's in the database
        entityManager.flush();
        // clear the cache otherwise finding the entity will not perform a select but get the entity from cache
        entityManager.clear();

        AndroidVariant variant = (AndroidVariant) variantDao.find(id);

        assertThat(variant.getId()).isEqualTo(id);

        av.setGoogleKey("NEW_KEY");
        variantDao.update(av);

        entityManager.flush();
        entityManager.clear();

        variant = (AndroidVariant) variantDao.find(id);

        assertThat(variant.getGoogleKey()).isEqualTo("NEW_KEY");

        assertThat(av.getId()).isEqualTo(id);
    }

    @Test
    public void deleteVariantIncludingInstallations() {

        final AndroidVariant av = new AndroidVariant();
        av.setGoogleKey("KEY");
        av.setDeveloper("admin");
        final String uuid  = av.getVariantID();

        variantDao.create(av);

        AndroidVariant queriedVariant = (AndroidVariant) variantDao.findByVariantID(uuid);
        assertThat(queriedVariant).isNotNull();
        assertThat(queriedVariant.getGoogleKey()).isEqualTo("KEY");

        Installation androidInstallation1 = new Installation();
        androidInstallation1.setDeviceToken("1234543212232301234567890012345678900123456789001234567890012345678900123456789001234567890012345678");
        final HashSet<Category> categories = new HashSet<Category>();
        categories.add(new Category("X"));
        categories.add(new Category("Y"));
        androidInstallation1.setCategories(categories);
        installationDao.create(androidInstallation1);

        androidInstallation1.setVariant(queriedVariant);
        variantDao.update(queriedVariant);

        Installation storedInstallation =  installationDao.find(androidInstallation1.getId());
        assertThat(storedInstallation.getId()).isEqualTo(androidInstallation1.getId());

        variantDao.delete(queriedVariant);
        entityManager.flush();
        entityManager.clear();
        assertThat(variantDao.findByVariantID(uuid)).isNull();

        // Installation should be gone...
        assertThat(installationDao.find(androidInstallation1.getId())).isNull();
    }


    @Test
    public void createDifferentVariantTypes() {
        AndroidVariant av = new AndroidVariant();
        av.setGoogleKey("KEY");
        av.setDeveloper("admin");

        variantDao.create(av);

        // flush to be sure that it's in the database
        entityManager.flush();
        // clear the cache otherwise finding the entity will not perform a select but get the entity from cache
        entityManager.clear();


        APNsVariant iOS = new APNsVariant();
        iOS.setCertificate("test".getBytes());
        iOS.setPassphrase("secret");
        final String iOSid = iOS.getVariantID();

        variantDao.create(iOS);
        // flush to be sure that it's in the database
        entityManager.flush();
        // clear the cache otherwise finding the entity will not perform a select but get the entity from cache
        entityManager.clear();

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
        //given
        AndroidVariant av = new AndroidVariant();
        av.setGoogleKey("KEY");
        av.setDeveloper("admin");
        final String variantID  = av.getVariantID();
        variantDao.create(av);

        //when
        assertThat(variantDao.existsVariantIDForDeveloper(variantID, "admin")).isEqualTo(true);
        assertThat(variantDao.existsVariantIDForDeveloper(variantID, "foo")).isEqualTo(false);
    }

    @Test
    public void shouldDetectThatVariantIdExistsForAdmin() {
        //given
        AndroidVariant av = new AndroidVariant();
        av.setGoogleKey("KEY");
        av.setDeveloper("foo");
        final String variantID  = av.getVariantID();
        variantDao.create(av);

        //when
        assertThat(variantDao.existsVariantIDForAdmin(variantID)).isEqualTo(true);
        assertThat(variantDao.existsVariantIDForDeveloper(variantID, "foo")).isEqualTo(true);
    }
}
