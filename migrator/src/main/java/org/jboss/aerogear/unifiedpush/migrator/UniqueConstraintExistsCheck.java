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

import liquibase.database.Database;
import liquibase.exception.CustomPreconditionErrorException;
import liquibase.exception.CustomPreconditionFailedException;
import liquibase.precondition.CustomPrecondition;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.structure.core.Column;
import liquibase.structure.core.UniqueConstraint;

public class UniqueConstraintExistsCheck implements CustomPrecondition {

    private String tableName;
    private String columnNames;
    private String constraintName;

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public void setColumnNames(String columnNames) {
        this.columnNames = columnNames;
    }

    public void setConstraintName(String constraintName) {
        this.constraintName = constraintName;
    }

    @Override
    public void check(Database database) throws CustomPreconditionFailedException, CustomPreconditionErrorException {
        Column column = new Column(this.columnNames);
        UniqueConstraint uniqueConstraint = new UniqueConstraint(this.constraintName, null, null, this.tableName, column);
        boolean markFailed = false;
        try {
            if (!SnapshotGeneratorFactory.getInstance().has(uniqueConstraint, database)) {
                markFailed = true;
            }
        } catch (Exception e) {
            throw new CustomPreconditionErrorException("custom precondition check errored", e);
        }

        if (markFailed) {
            throw new CustomPreconditionFailedException(this.constraintName + " doesn't exist");
        }
    }
}
