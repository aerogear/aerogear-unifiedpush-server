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

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.jboss.aerogear.unifiedpush.system.ConfigurationEnvironment;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.zaxxer.hikari.HikariDataSource;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { JPAConfig.class })
@DirtiesContext(classMode = ClassMode.BEFORE_CLASS)
// When running with maven we can't ensure no other test was already
// initialized spring context with System.setProperty
public class JPAConfigTest {
	@Autowired
	private DataSource datasource;

	static {
		String dirPath = System.getProperty("java.io.tmpdir") + File.separator + JPAConfigTest.class.getName();

		List<String> lines = new ArrayList<>();
		lines.add("dataSource.prepStmtCacheSize=500");

		try {
			Files.deleteIfExists(Paths.get(dirPath + File.separator + "db.properties"));

			Files.createDirectories(Paths.get(dirPath));
			Files.write(Paths.get(dirPath + File.separator + "db.properties"), lines, StandardOpenOption.CREATE_NEW);
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.setProperty(ConfigurationEnvironment.CONF_DIR, dirPath);
	}

	@Test
	public void testConfig() {
		assertEquals(((HikariDataSource) datasource).getUsername(), "sa");
		assertEquals(((HikariDataSource) datasource).getDataSourceProperties().getProperty("prepStmtCacheSize"), "500");
	}
}
