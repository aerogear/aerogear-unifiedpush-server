/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.unifiedpush.message.configuration;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TestSenderConfigurationProvider {

    private SenderConfigurationProvider provider = new SenderConfigurationProvider();

    @Test
    public void testAndroidConfigurationSanitization() {
        try {
            System.setProperty("aerogear.android.batchSize", "1005");
            SenderConfiguration configuration = provider.produceAndroidConfiguration();
            assertEquals(10, configuration.batchesToLoad());
            assertEquals(1000, configuration.batchSize());
        } finally {
            System.clearProperty("aerogear.android.batchSize");
        }
    }
}
