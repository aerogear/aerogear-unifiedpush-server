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
package org.jboss.aerogear.unifiedpush.jpa;

import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.MySQLDialect;
import org.hibernate.exception.JDBCConnectionException;
import org.hibernate.service.jdbc.dialect.spi.DialectResolver;
import org.jboss.aerogear.unifiedpush.utils.AeroGearLogger;

import java.sql.DatabaseMetaData;

/**
 * A Hibernate dialect resolver to allow for a custom Mysql5 dialect
 *
 * @see Mysql5BitBooleanDialect
 */
public class MysqlDialectResolver implements DialectResolver {
    private final AeroGearLogger logger = AeroGearLogger.getInstance(MysqlDialectResolver.class);

    @Override
    public Dialect resolveDialect(final DatabaseMetaData databaseMetaData) throws JDBCConnectionException {
        try {
            if ("MySQL".equals(databaseMetaData.getDatabaseProductName())) {
                return databaseMetaData.getDatabaseMajorVersion() >= 5 ? new Mysql5BitBooleanDialect() : new MySQLDialect();
            }
        } catch (final Exception e) {
            logger.severe("Could not get database name/version", e);
        }
        return null;
    }
}
