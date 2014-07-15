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
package org.jboss.aerogear.unifiedpush.api;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class PushApplicationTest {

    private PushApplication pushApplication;

    @Before
    public void setup() {
        pushApplication = new PushApplication();
        pushApplication.setDescription("desc");
        pushApplication.setDeveloper("Admin");
        pushApplication.setName("MyPushApp");
    }


    @Test
    public void pushApplicationValues() {
        assertThat(pushApplication.getDescription()).isEqualTo("desc");
        assertThat(pushApplication.getDeveloper()).isEqualTo("Admin");
        assertThat(pushApplication.getName()).isEqualTo("MyPushApp");

        assertThat(pushApplication.getPushApplicationID()).isNotNull();
        assertThat(pushApplication.getMasterSecret()).isNotNull();
    }


    @Test
    public void addVariant() {

        AndroidVariant av = new AndroidVariant();
        av.setGoogleKey("key");
        av.setName("Android Version");

        pushApplication.getVariants().add(av);
        assertThat(pushApplication.getVariants()).hasSize(1);
    }

}
