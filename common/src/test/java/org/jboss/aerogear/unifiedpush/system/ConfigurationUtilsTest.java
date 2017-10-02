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
package org.jboss.aerogear.unifiedpush.system;


import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { ConfigurationEnvironment.class })
@DirtiesContext(classMode = ClassMode.BEFORE_CLASS)
public class ConfigurationUtilsTest {
    private static final String TEST_PROPERTY_NAME = "ConfigurationUtilsTestProperty";

	@Autowired
	private ConfigurationEnvironment environment;

	static {
		String dirPath = System.getProperty("java.io.tmpdir") + File.separator + ConfigurationUtilsTest.class.getName();

		List<String> lines = new ArrayList<>();
		lines.add(ConfigurationEnvironment.PROP_MASTER_VERIFICATION + "=123456");

		try {
			Files.deleteIfExists(Paths.get(dirPath + File.separator + "environment.properties"));

			Files.createDirectories(Paths.get(dirPath));
			Files.write(Paths.get(dirPath + File.separator + "environment.properties"), lines, StandardOpenOption.CREATE_NEW);
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.setProperty("aerobase.config.dir", dirPath);
	}

	@Test
	public void testConfig() {
		assertEquals(environment.getProperties().get(ConfigurationEnvironment.PROP_MASTER_VERIFICATION), "123456");
	}

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
