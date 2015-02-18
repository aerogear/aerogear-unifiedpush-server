/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.unifiedpush.message.util;


import org.junit.After;
import org.junit.Test;
import static org.assertj.core.api.Assertions.*;
import java.util.Properties;

public class ConfigurationUtilsTest {

    private static final String TEST_PROPERTY_NAME = "ConfigurationUtilsTestProperty";

    @After
    public void cleanupTestProperty() {
        Properties properties = System.getProperties();
        properties.remove(TEST_PROPERTY_NAME);
    }

    @Test
    public void testExistingTryGetProperty(){
        System.setProperty(TEST_PROPERTY_NAME, "MyNiceValue");
        assertThat(ConfigurationUtils.tryGetProperty(TEST_PROPERTY_NAME)).isEqualTo("MyNiceValue");
    }

    @Test
    public void testNonExistingTryGetProperty(){
        assertThat(ConfigurationUtils.tryGetProperty(TEST_PROPERTY_NAME)).isNull();
    }

    @Test
    public void testExistingTryGetIntegerProperty() {
        System.setProperty(TEST_PROPERTY_NAME, "123456");
        assertThat(ConfigurationUtils.tryGetIntegerProperty(TEST_PROPERTY_NAME)).isEqualTo(123456);
    }

    @Test
    public void testNonExistingTryGetIntegerProperty() {
        assertThat(ConfigurationUtils.tryGetIntegerProperty(TEST_PROPERTY_NAME)).isNull();
    }

    @Test
    public void testNonExistingTryGetIntegerPropertyWithDefaultValue() {
        assertThat(ConfigurationUtils.tryGetIntegerProperty(TEST_PROPERTY_NAME, 123)).isEqualTo(123);
    }

}
