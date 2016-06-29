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
package org.jboss.aerogear.unifiedpush.migrator;

import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.CustomChangeException;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoriesMigration implements CustomTaskChange {
    private String confirmationMessage;

    @Override
    public String getConfirmationMessage() {
        return this.confirmationMessage;
    }

    @Override
    public void setUp() throws SetupException {

    }

    @Override
    public void setFileOpener(ResourceAccessor resourceAccessor) {

    }

    @Override
    public ValidationErrors validate(Database database) {
        return null;
    }

    @Override
    public void execute(Database database) throws CustomChangeException {
        try (Connection conn = ((JdbcConnection) (database.getConnection())).getWrappedConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            boolean hasTable = false;
            try (ResultSet tables = metaData.getTables(null, null, null, null)) {
                while (tables.next()) {
                    String tableName = tables.getString(3);
                    if ("Installation_categories".equals(tableName) || "installation_categories".equals(tableName)) {
                        hasTable = true;
                    }
                }
            }
            if (!hasTable) {
                this.confirmationMessage = "table doesn't exists, skipping";
                return;
            }
            List<String> categories = new ArrayList<String>();
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("select distinct categories from Installation_categories")) {
                while (rs.next()) {
                    String category = rs.getString(1);
                    categories.add(category);
                }
            }
            conn.setAutoCommit(false);
            long categoryCounter = 1;
            try (PreparedStatement categoriesStatement = conn.prepareStatement("insert into category (id, name) values (?, ?)")) {
                for (String category : categories) {
                    categoriesStatement.setLong(1, categoryCounter);
                    categoriesStatement.setString(2, category);
                    categoriesStatement.executeUpdate();
                    categoryCounter++;
                }
            }
            if ("mysql".equals(database.getShortName())) {
                try (PreparedStatement sequenceStatement = conn.prepareStatement("update category_seq set next_val = ? where next_val = ?")) {
                    sequenceStatement.setLong(1, categoryCounter + 1);
                    sequenceStatement.setLong(2, categoryCounter);
                    sequenceStatement.executeUpdate();
                }
            } else if ("postgresql".equals(database.getShortName())) {
                try (Statement statement = conn.createStatement()) {
                    statement.executeUpdate("alter sequence category_seq restart with " + categoryCounter + 1);
                }
            }
            conn.commit();
            this.confirmationMessage = categoryCounter + " categories migrated successfully";
        } catch (Exception e) {
            throw new CustomChangeException(e);
        }
    }
}
