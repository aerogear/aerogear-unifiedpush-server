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
import org.jboss.aerogear.unifiedpush.api.PushApplication;
import org.jboss.aerogear.unifiedpush.api.VariantType;
import org.jboss.aerogear.unifiedpush.dao.PushApplicationDao;
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
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(Arquillian.class)
public class PushApplicationDaoTest {

    @Inject
    private EntityManager entityManager;
    @Inject
    private PushApplicationDao pushApplicationDao;

    @Deployment
    public static JavaArchive createDeployment() {
        return DaoDeployment.createDeployment();
    }

    @Before
    public void setUp() {
        // start the shindig
        entityManager.getTransaction().begin();
    }

    @After
    public void tearDown() {
        entityManager.getTransaction().rollback();
    }

    @Rule
    public EmbeddedDbTesterRule testDb = new EmbeddedDbTesterRule("PushApplications.xml");

    @Test
    public void findAllForDeveloper() throws Exception {
        assertThat(pushApplicationDao.findAllForDeveloper("Admin", 0, 10).getResultList()).hasSize(2);
        assertThat(pushApplicationDao.findAllForDeveloper("Dave The Drummer", 0, 10).getResultList()).hasSize(1);
        assertThat(pushApplicationDao.findAllForDeveloper("Dave The Drummer", 0, 10).getResultList()).extracting("name").containsOnly("Push App 3");
        assertThat(pushApplicationDao.findAllForDeveloper("Admin The Drummer", 0, 10).getResultList()).isEmpty();

        assertThat(pushApplicationDao.getNumberOfPushApplicationsForDeveloper("Dave The Drummer")).isEqualTo(1);
        assertThat(pushApplicationDao.getNumberOfPushApplicationsForDeveloper("Admin")).isEqualTo(2);

        // check all:
        assertThat(pushApplicationDao.getNumberOfPushApplicationsForDeveloper()).isEqualTo(3);
        assertThat(pushApplicationDao.findAll(0, 10).getCount()).isEqualTo(3);

        // check exact:
        assertThat(pushApplicationDao.findAllByPushApplicationID("123").getName()).isEqualTo("Push App 2");
    }

    @Test
    public void findAllIDsForDeveloper() {
        assertThat(pushApplicationDao.findAllPushApplicationIDsForDeveloper("Admin")).hasSize(2);
        assertThat(pushApplicationDao.findAllPushApplicationIDsForDeveloper("Dave The Drummer")).hasSize(1);
        assertThat(pushApplicationDao.findAllPushApplicationIDsForDeveloper("Admin The Drummer")).isEmpty();

        // check all:
        assertThat(pushApplicationDao.getNumberOfPushApplicationsForDeveloper()).isEqualTo(3);
        assertThat(pushApplicationDao.findAll(0, 10).getCount()).isEqualTo(3);
    }

    @Test
    public void findByPushApplicationIDForDeveloper() {
        final String pushApplicationID1 = "888";

        assertThat(pushApplicationDao.findByPushApplicationIDForDeveloper(pushApplicationID1, "Admin")).isNotNull();

        assertThat(pushApplicationDao.findByPushApplicationIDForDeveloper(pushApplicationID1, "Admin").getName()).isEqualTo("Push App 1");

        assertThat(pushApplicationDao.findByPushApplicationIDForDeveloper("1234", "Admin")).isNull();
        assertThat(pushApplicationDao.findByPushApplicationIDForDeveloper(pushApplicationID1, "FooBar")).isNull();

    }

    @Test
    public void findByPushApplicationID() {
        final String pushApplicationID1 = "888";

        assertThat(pushApplicationDao.findByPushApplicationID(pushApplicationID1)).isNotNull();
        assertThat(pushApplicationDao.findByPushApplicationID(pushApplicationID1).getName()).isEqualTo("Push App 1");
        assertThat(pushApplicationDao.findByPushApplicationID("13245632")).isNull();
    }

    @Test
    public void updatePushApplication() {
        final String pushApplicationID1 = "888";

        final PushApplication pushApplication1 = pushApplicationDao.findByPushApplicationID(pushApplicationID1);
        assertThat(pushApplication1).isNotNull();
        assertThat(pushApplication1.getName()).isEqualTo("Push App 1");

        pushApplication1.setName("Cool Push App 1");
        pushApplicationDao.update(pushApplication1);

        assertThat(pushApplicationDao.findByPushApplicationID(pushApplicationID1)).isNotNull();
        assertThat(pushApplicationDao.findByPushApplicationID(pushApplicationID1).getName()).isEqualTo("Cool Push App 1");
    }

    @Test
    public void updateAndDeletePushApplication() {
        final String pushApplicationID1 = "888";

        assertThat(pushApplicationDao.findByPushApplicationID(pushApplicationID1)).isNotNull();
        assertThat(pushApplicationDao.findByPushApplicationID(pushApplicationID1).getName()).isEqualTo("Push App 1");


        final PushApplication pushApplication1 = pushApplicationDao.findByPushApplicationID(pushApplicationID1);
        pushApplication1.setName("Cool Push App 1");
        pushApplicationDao.update(pushApplication1);

        assertThat(pushApplicationDao.findByPushApplicationID(pushApplicationID1)).isNotNull();
        assertThat(pushApplicationDao.findByPushApplicationID(pushApplicationID1).getName()).isEqualTo("Cool Push App 1");

        pushApplicationDao.delete(pushApplication1);
        assertThat(pushApplicationDao.findByPushApplicationID(pushApplicationID1)).isNull();
    }

    @Test
    public void pushApplicationIDUnmodifiedAfterUpdate() {
        final String pushApplicationID1 = "888";

        assertThat(pushApplicationDao.findByPushApplicationID(pushApplicationID1)).isNotNull();
        assertThat(pushApplicationDao.findByPushApplicationID(pushApplicationID1).getName()).isEqualTo("Push App 1");


        final PushApplication pushApplication1 = pushApplicationDao.findByPushApplicationID(pushApplicationID1);
        pushApplication1.setName("Cool Push App 1");
        pushApplicationDao.update(pushApplication1);

        assertThat(pushApplicationDao.findByPushApplicationID(pushApplicationID1)).isNotNull();
        assertThat(pushApplicationDao.findByPushApplicationID(pushApplicationID1).getPushApplicationID()).isEqualTo(pushApplicationID1);
    }

    @Test
    public void primaryKeyUnmodifiedAfterUpdate() {
        final String id = "1";
        PushApplication pa = pushApplicationDao.find(id);

        assertThat(pa.getId()).isEqualTo(id);

        final PushApplication pushApplication1 = pushApplicationDao.findByPushApplicationID(pa.getPushApplicationID());
        pushApplication1.setName("Cool Push App 1");
        pushApplicationDao.update(pushApplication1);

        entityManager.flush();
        entityManager.clear();

        pa = pushApplicationDao.find(id);

        assertThat(pa.getName()).isEqualTo("Cool Push App 1");
    }

    @Test
    public void deletePushApplicationIncludingVariantAndInstallations() {
        final String id = "888";

        final PushApplication pa = pushApplicationDao.findByPushApplicationID(id);

        pushApplicationDao.delete(pa);
        // flush to be sure that it's in the database
        entityManager.flush();
        // clear the cache otherwise finding the entity will not perform a select but get the entity from cache
        entityManager.clear();

        // Installation should be gone:
        assertThat(entityManager.find(Installation.class, "1")).isNull();

        // Variant should be gone:
        assertThat(entityManager.find(AndroidVariant.class, "1")).isNull();

        // PushApp should be gone:
        assertThat(pushApplicationDao.find(id)).isNull();
    }

    @Test
    public void shouldCountInstallations() {
        final Map<String, Long> result = pushApplicationDao.countInstallationsByType("888");

        assertThat(result).isNotEmpty();
        assertThat(result.get("1")).isEqualTo(2L);
        assertThat(result.get("3")).isEqualTo(1L);
        assertThat(result.get(VariantType.ANDROID.getTypeName())).isEqualTo(2L);
    }

    @Test
    public void shouldFindPushApplicationNameAndIDBasedOnVariantID() {

        //when
        final List<PushApplication> applications = pushApplicationDao.findByVariantIds(Arrays.asList("1"));

        //then
        assertThat(applications).isNotEmpty();
        assertThat(applications.size()).isEqualTo(1);

        final PushApplication application = applications.iterator().next();
        assertThat(application.getName()).isEqualTo("Push App 1");
        assertThat(application.getVariants()).isNotEmpty();
        assertThat(application.getVariants().size()).isEqualTo(1);
        assertThat(application.getVariants().iterator().next().getId()).isEqualTo("1");
    }
}
