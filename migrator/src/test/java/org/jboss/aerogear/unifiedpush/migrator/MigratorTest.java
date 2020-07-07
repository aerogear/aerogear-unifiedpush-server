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
package org.jboss.aerogear.unifiedpush.migrator;


import static org.junit.Assume.assumeTrue;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.resource.FileSystemResourceAccessor;
import liquibase.resource.ResourceAccessor;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class MigratorTest {

    private Database database;
    private ResourceAccessor resourceAccessor;
    private EmbeddedMysqlDatabase embeddedMysqlDatabase;
    
    @Before
    public void init() throws Exception {
        initResourceAccessor();
        initDatabase();
    }

    private void initResourceAccessor() {
        String baseDir = "target/classes/liquibase";
        resourceAccessor = new FileSystemResourceAccessor(baseDir);
    }

    private void initDatabase() throws Exception {
        String url = System.getProperty("jdbc.url");
        String username = System.getProperty("jdbc.username", "unifiedpush");
        String password = System.getProperty("jdbc.password", "unifiedpush");

        if (url == null) {
            assumeTrue("Skip test on Windows, because mysql-connector-mxj has issue on new versions, see http://stackoverflow.com/questions/9520536/missingresourceexception-running-mxj-for-mysql for more details.", !org.apache.commons.lang3.SystemUtils.IS_OS_WINDOWS);

            embeddedMysqlDatabase = new EmbeddedMysqlDatabase();
            embeddedMysqlDatabase.start("unifiedpush");

            url = embeddedMysqlDatabase.getUrl();
            username = "root";
            password = "";
        }

        database = DatabaseFactory.getInstance().openDatabase(url, username, password, null, resourceAccessor);
    }

    @After
    public void close() throws Exception {
        if (database != null) {
            database.close();
        }
        if (embeddedMysqlDatabase != null) {
            embeddedMysqlDatabase.close();
        }
    }

    @Test
    public void shouldCreateDatabaseFromScratch() throws Exception {
        String changeLogFile = "master.xml";

        Liquibase liquibase = new Liquibase(changeLogFile, resourceAccessor, database);
        liquibase.dropAll();
        liquibase.update("");
    }

}