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

import liquibase.change.custom.CustomSqlChange;
import liquibase.database.Database;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.CustomChangeException;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.InsertStatement;
import liquibase.statement.core.RawSqlStatement;
import liquibase.statement.core.UpdateStatement;
import liquibase.structure.core.Table;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class CategoriesMigration implements CustomSqlChange {
    private String confirmationMessage;

    @Override
    public SqlStatement[] generateStatements(Database database) throws CustomChangeException {


        ArrayList<SqlStatement> sqlStatements = new ArrayList<SqlStatement>();


        try {
            Table tableA = new Table(null, null, "Installation_categories");
            Table tableB = new Table(null, null, "installation_categories");

            SnapshotGeneratorFactory instance = SnapshotGeneratorFactory.getInstance();
            if (!instance.has(tableA, database) || !instance.has(tableB, database)) {
                // table doesn't exist, aborting
                this.confirmationMessage = "table doesn't exists, skipping";
                return unwrap(sqlStatements);
            }
            Connection conn = ((JdbcConnection) (database.getConnection())).getWrappedConnection();
            ResultSet rs = conn.createStatement().executeQuery("select distinct categories from installation_categories");
            List<String> categories = new ArrayList<String>();
            while (rs.next()) {
                String category = rs.getString(1);
                categories.add(category);
            }
            rs.close();
            long categoryCounter = 1;
            for (String category : categories) {
                InsertStatement categoryInsert = new InsertStatement(null, null, "category")
                        .addColumnValue("id", categoryCounter++)
                        .addColumnValue("name", category);
                sqlStatements.add(categoryInsert);
            }

            if ("mysql".equals(database.getShortName())) {
                UpdateStatement updateStatement = new UpdateStatement(null, null, "category_seq")
                        .addNewColumnValue("next_val", categoryCounter + 1)
                        .addWhereColumnName("next_val")
                        .addWhereParameter(categoryCounter);
                sqlStatements.add(updateStatement);
            } else if ("postgresql".equals(database.getShortName())) {
                RawSqlStatement rawSqlStatement = new RawSqlStatement("alter sequence category_seq start " + categoryCounter + 1);
                sqlStatements.add(rawSqlStatement);
            }
            this.confirmationMessage = categoryCounter + " categories migrated successfully";
        } catch (Exception e) {
            throw new CustomChangeException(e);
        }
        return unwrap(sqlStatements);
    }

    private SqlStatement[] unwrap(ArrayList<SqlStatement> sqlStatements) {
        return sqlStatements.toArray(new SqlStatement[sqlStatements.size()]);
    }

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
}
