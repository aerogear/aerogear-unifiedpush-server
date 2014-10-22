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
package org.jboss.aerogear.unifiedpush.jpa.cdi;

import liquibase.Contexts;
import liquibase.Liquibase;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.changelog.RanChangeSet;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.logging.LogFactory;
import liquibase.logging.LogLevel;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.servicelocator.ServiceLocator;

import java.sql.Connection;
import java.text.MessageFormat;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Uses liquibase to update the schema of the database to the latest version.
 */
public class SchemaUpdater {
    private static final Logger logger = Logger.getLogger(SchemaUpdater.class.getName());

    public static final String CHANGELOG = "db.changelog-master.xml";

    public void update(Connection connection) {
        logger.finest("Starting database update");

        try {
            Liquibase liquibase = getLiquibase(connection);

            List<ChangeSet> changeSets = liquibase.listUnrunChangeSets((Contexts) null);
            if (!changeSets.isEmpty()) {
                if (logger.isLoggable(Level.FINEST)) {
                    List<RanChangeSet> ranChangeSets = liquibase.getDatabase().getRanChangeSetList();
                    logger.finest(MessageFormat.format("Updating database from {0} to {1}", ranChangeSets.get(ranChangeSets.size() - 1).getId(), changeSets.get(changeSets.size() - 1).getId()));
                } else {
                    logger.finest("Updating database");
                }

                liquibase.update((Contexts) null);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to update database", e);
        }
        logger.finest("Completed database update");
    }

    private Liquibase getLiquibase(Connection connection) throws Exception {
        ServiceLocator sl = ServiceLocator.getInstance();

        if (!System.getProperties().containsKey("liquibase.scan.packages")) {
            if (sl.getPackages().remove("liquibase.core")) {
                sl.addPackageToScan("liquibase.core.xml");
            }

            if (sl.getPackages().remove("liquibase.parser")) {
                sl.addPackageToScan("liquibase.parser.core.xml");
            }

            if (sl.getPackages().remove("liquibase.serializer")) {
                sl.addPackageToScan("liquibase.serializer.core.xml");
            }

            sl.getPackages().remove("liquibase.ext");
            sl.getPackages().remove("liquibase.sdk");
        }

        LogFactory.setInstance(new LogWrapper());
        Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
        return new Liquibase(CHANGELOG, new ClassLoaderResourceAccessor(getClass().getClassLoader()), database);
    }

    private static class LogWrapper extends LogFactory {

        private liquibase.logging.Logger logger = new liquibase.logging.Logger() {
            @Override
            public void setName(String name) {
            }

            @Override
            public void setLogLevel(String level) {
            }

            @Override
            public void setLogLevel(LogLevel level) {
            }

            @Override
            public void setLogLevel(String logLevel, String logFile) {
            }

            @Override
            public void severe(String message) {
                SchemaUpdater.logger.severe(message);
            }

            @Override
            public void severe(String message, Throwable e) {
                SchemaUpdater.logger.log(Level.SEVERE, message, e);
            }

            @Override
            public void warning(String message) {
                SchemaUpdater.logger.warning(message);
            }

            @Override
            public void warning(String message, Throwable e) {
                SchemaUpdater.logger.log(Level.INFO, message, e);
            }

            @Override
            public void info(String message) {
                SchemaUpdater.logger.info(message);
            }

            @Override
            public void info(String message, Throwable e) {
                SchemaUpdater.logger.log(Level.INFO, message, e);
            }

            @Override
            public void debug(String message) {
                SchemaUpdater.logger.fine(message);
            }

            @Override
            public LogLevel getLogLevel() {
                if (SchemaUpdater.logger.isLoggable(Level.FINE) || SchemaUpdater.logger.isLoggable(Level.FINEST)) {
                    return LogLevel.DEBUG;
                } else if (SchemaUpdater.logger.isLoggable(Level.INFO)) {
                    return LogLevel.INFO;
                } else {
                    return LogLevel.WARNING;
                }
            }

            @Override
            public void debug(String message, Throwable e) {
                SchemaUpdater.logger.log(Level.FINEST, message, e);
            }

            @Override
            public void setChangeLog(DatabaseChangeLog databaseChangeLog) {
            }

            @Override
            public void setChangeSet(ChangeSet changeSet) {
            }

            @Override
            public int getPriority() {
                return 0;
            }
        };

        @Override
        public liquibase.logging.Logger getLog(String name) {
            return logger;
        }

        @Override
        public liquibase.logging.Logger getLog() {
            return logger;
        }

    }
}
