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

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.naming.InitialContext;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * CDI Utility class, which produces the entity manager.
 */
public final class Factory {
    private final Logger logger = Logger.getLogger(Factory.class.getName());

    @Inject
    private SchemaUpdater schemaUpdater;

    @Produces
    @ApplicationScoped
    private EntityManager produceEntityManager() {
        Connection connection = getConnection();
        final boolean developmentDatabase = isDevelopmentDatabase(connection);
        if (!developmentDatabase) {
            schemaUpdater.update(connection);
        }
        close(connection);
        return createEntityManager(developmentDatabase);
    }

    private Connection getConnection() {
        try {
            String dataSourceLookup = "java:jboss/datasources/UnifiedPushDS";
            DataSource dataSource = (DataSource) new InitialContext().lookup(dataSourceLookup);
            return dataSource.getConnection();
        } catch (Exception e) {
            throw new RuntimeException("Failed to connect to database", e);
        }
    }

    private void close(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                logger.log(Level.FINEST, "error closing database connection", e);
            }
        }
    }

    private EntityManager createEntityManager(boolean developmentDatabase) {
        synchronized (this) {
            String connection = developmentDatabase ? "unifiedpush-dev" : "unifiedpush-default";
            final EntityManagerFactory factory = Persistence.createEntityManagerFactory(connection);
            return factory.createEntityManager();
        }
    }

    private boolean isDevelopmentDatabase(Connection connection ) {
        boolean result = false;
        try {
            result = "H2".equals(connection.getMetaData().getDatabaseProductName());
        } catch (SQLException e) {
            logger.warning("error in determining database type, assuming production environment");
        }

        return result;
    }
}