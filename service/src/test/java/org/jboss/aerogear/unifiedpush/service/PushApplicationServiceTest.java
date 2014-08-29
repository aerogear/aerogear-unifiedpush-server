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
package org.jboss.aerogear.unifiedpush.service;

import org.apache.openejb.jee.Beans;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Module;
import org.jboss.aerogear.unifiedpush.api.PushApplication;
import org.jboss.aerogear.unifiedpush.jpa.dao.impl.JPAPushApplicationDao;
import org.jboss.aerogear.unifiedpush.service.impl.PushApplicationServiceImpl;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(ApplicationComposer.class)
public class PushApplicationServiceTest extends AbstractBaseServiceTest {

    @Inject
    private PushApplicationService pushApplicationService;

    @Module
    public Beans getBeans() {
        final Beans beans = new Beans();
        beans.addManagedClass(PushApplicationServiceImpl.class);
        beans.addManagedClass(JPAPushApplicationDao.class);

        return beans;
    }

    @Test
    public void addPushApplication() {
        PushApplication pa = new PushApplication();
        pa.setName("EJB Container");
        final String uuid = UUID.randomUUID().toString();
        pa.setPushApplicationID(uuid);

        pushApplicationService.addPushApplication(pa);

        PushApplication stored = pushApplicationService.findByPushApplicationID(uuid);
        assertThat(stored).isNotNull();
        assertThat(stored.getId()).isNotNull();
        assertThat(pa.getName()).isEqualTo(stored.getName());
        assertThat(pa.getPushApplicationID()).isEqualTo(stored.getPushApplicationID());
    }

    @Test
    public void updatePushApplication() {
        PushApplication pa = new PushApplication();
        pa.setName("EJB Container");
        final String uuid = UUID.randomUUID().toString();
        pa.setPushApplicationID(uuid);

        pushApplicationService.addPushApplication(pa);

        PushApplication stored = pushApplicationService.findByPushApplicationID(uuid);
        assertThat(stored).isNotNull();

        stored.setName("FOO");
        pushApplicationService.updatePushApplication(stored);
        stored = pushApplicationService.findByPushApplicationID(uuid);
        assertThat("FOO").isEqualTo(stored.getName());
    }

    @Test
    public void findByPushApplicationID() {
        PushApplication pa = new PushApplication();
        pa.setName("EJB Container");
        final String uuid = UUID.randomUUID().toString();
        pa.setPushApplicationID(uuid);

        pushApplicationService.addPushApplication(pa);

        PushApplication stored = pushApplicationService.findByPushApplicationID(uuid);
        assertThat(stored).isNotNull();
        assertThat(stored.getId()).isNotNull();
        assertThat(pa.getName()).isEqualTo(stored.getName());
        assertThat(pa.getPushApplicationID()).isEqualTo(stored.getPushApplicationID());

        stored = pushApplicationService.findByPushApplicationID("123");
        assertThat(stored).isNull();

    }

    @Test
    public void findAllPushApplicationsForDeveloper() {

        assertThat(pushApplicationService.findAllPushApplicationsForDeveloper("admin")).isEmpty();

        PushApplication pa = new PushApplication();
        pa.setName("EJB Container");
        final String uuid = UUID.randomUUID().toString();
        pa.setPushApplicationID(uuid);
        pa.setDeveloper("admin");

        pushApplicationService.addPushApplication(pa);

        assertThat(pushApplicationService.findAllPushApplicationsForDeveloper("admin")).isNotEmpty();
        assertThat(pushApplicationService.findAllPushApplicationsForDeveloper("admin")).hasSize(1);
    }

    @Test
    public void removePushApplication() {
        PushApplication pa = new PushApplication();
        pa.setName("EJB Container");
        final String uuid = UUID.randomUUID().toString();
        pa.setPushApplicationID(uuid);
        pa.setDeveloper("admin");

        pushApplicationService.addPushApplication(pa);

        assertThat(pushApplicationService.findAllPushApplicationsForDeveloper("admin")).isNotEmpty();
        assertThat(pushApplicationService.findAllPushApplicationsForDeveloper("admin")).hasSize(1);

        pushApplicationService.removePushApplication(pa);

        assertThat(pushApplicationService.findAllPushApplicationsForDeveloper("admin")).isEmpty();
        assertThat(pushApplicationService.findByPushApplicationID(uuid)).isNull();
    }

    @Test
    public void findByPushApplicationIDForDeveloper() {
        PushApplication pa = new PushApplication();
        pa.setName("EJB Container");
        final String uuid = UUID.randomUUID().toString();
        pa.setPushApplicationID(uuid);
        pa.setDeveloper("admin");

        pushApplicationService.addPushApplication(pa);

        PushApplication queried =  pushApplicationService.findByPushApplicationIDForDeveloper(uuid, "admin");
        assertThat(queried).isNotNull();
        assertThat(uuid).isEqualTo(queried.getPushApplicationID());

        assertThat(pushApplicationService.findByPushApplicationIDForDeveloper(uuid, "admin2")).isNull();
        assertThat(pushApplicationService.findByPushApplicationIDForDeveloper("123-3421", "admin")).isNull();
    }
}
