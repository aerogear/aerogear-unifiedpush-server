/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.unifiedpush.service;

import org.jboss.aerogear.unifiedpush.api.PushApplication;
import org.jboss.aerogear.unifiedpush.test.archive.UnifiedPushArchive;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(Arquillian.class)
public class PushApplicationServiceTest6 extends AbstractBaseServiceTest {

    @Override
    protected void specificSetup() {
        // noop
    }


    @Deployment
    public static WebArchive archive() {
        return UnifiedPushArchive.forTestClass(PushApplicationServiceTest6.class)
                .withUtils()
                .withMessageModel()
                .withMockito()
                .withMessaging()
                .withDAOs()
                .withServices().forServiceTests()
                .withApi()
                .withUtils()
                .addClass(AbstractBaseServiceTest.class)
                .addClass(EntityManagerProducer.class)
                //I think arquillian is drunk
                .addMavenDependencies("org.assertj:assertj-core")

                .as(WebArchive.class);
    }


    @Test
    public void removePushApplication() {

        PushApplication pa = new PushApplication();
        pa.setName("EJB Container");
        final String uuid = UUID.randomUUID().toString();
        pa.setPushApplicationID(uuid);
        pa.setDeveloper("admin");

        pushApplicationService.addPushApplication(pa);

        assertThat(searchApplicationService.findAllPushApplicationsForDeveloper(0, 10).getResultList()).isNotEmpty();
        assertThat(searchApplicationService.findAllPushApplicationsForDeveloper(0, 10).getResultList()).hasSize(1);

        pushApplicationService.removePushApplication(pa);

        assertThat(searchApplicationService.findAllPushApplicationsForDeveloper(0, 10).getResultList()).isEmpty();
        assertThat(pushApplicationService.findByPushApplicationID(uuid)).isNull();
    }


}
