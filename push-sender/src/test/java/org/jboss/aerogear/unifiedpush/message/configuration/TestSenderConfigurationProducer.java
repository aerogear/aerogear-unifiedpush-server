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

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.jboss.aerogear.unifiedpush.api.VariantType;
import org.jboss.aerogear.unifiedpush.message.sender.SenderType;
import org.jboss.aerogear.unifiedpush.message.sender.SenderTypeLiteral;
import org.jboss.aerogear.unifiedpush.test.archive.UnifiedPushArchive;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class TestSenderConfigurationProducer {

    @Deployment
    public static WebArchive archive() {
        return UnifiedPushArchive.forTestClass(TestSenderConfigurationProducer.class)
                    .withApi()
                    .withUtils()
                    .addPackage(SenderConfiguration.class.getPackage())
                    .addClasses(SenderType.class, SenderTypeLiteral.class)
                .as(WebArchive.class);
    }

    @Inject @Any
    private Instance<SenderConfiguration> senderConfiguration;

    @Test
    public void testAndroid() {
        try {
            System.setProperty("aerogear.android.batchSize", "999");
            SenderConfiguration configuration = senderConfiguration.select(new SenderTypeLiteral(VariantType.ANDROID)).get();
            assertEquals(10, configuration.batchesToLoad());
            assertEquals(999, configuration.batchSize());
        } finally {
            System.clearProperty("aerogear.android.batchSize");
        }
    }

    @Test
    public void testIOS() {
        try {
            System.setProperty("aerogear.ios.batchSize", "1");
            SenderConfiguration configuration = senderConfiguration.select(new SenderTypeLiteral(VariantType.IOS)).get();
            assertEquals(3, configuration.batchesToLoad());
            assertEquals(1, configuration.batchSize());
        } finally {
            System.clearProperty("aerogear.ios.batchSize");
        }
    }
}
