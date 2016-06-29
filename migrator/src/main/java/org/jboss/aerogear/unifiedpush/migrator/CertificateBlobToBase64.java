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
import liquibase.statement.SqlStatement;
import liquibase.statement.core.UpdateStatement;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class CertificateBlobToBase64 implements CustomSqlChange {
    private String confirmationMessage = "";

    @Override
    public SqlStatement[] generateStatements(Database database) throws CustomChangeException {
        List<SqlStatement> statements = new ArrayList<>();

        Connection conn = ((JdbcConnection) (database.getConnection())).getWrappedConnection();

        try {
            conn.setAutoCommit(false);
            ResultSet resultSet = conn.createStatement().executeQuery("SELECT id, certificate from ios_variant");
            while (resultSet.next()) {
                String id = resultSet.getString("id");
                Blob blob = resultSet.getBlob("certificate");
                InputStream certificate = blob.getBinaryStream();
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                int bytesRead = -1;
                byte[] buffer = new byte[1024];
                while ((bytesRead = certificate.read(buffer)) != -1) {
                    stream.write(buffer, 0, bytesRead);
                }
                final String certificateData = Base64.getEncoder().encodeToString(stream.toByteArray());

                UpdateStatement updateStatement = new UpdateStatement(null, null, "ios_variant")
                        .addNewColumnValue("cert_data", certificateData)
                        .setWhereClause("id='" + id + "'");
                statements.add(updateStatement);

            }
            conn.commit();

            if (!statements.isEmpty()) {
                confirmationMessage = "updated certificate data successfully";
            }

            return statements.toArray(new SqlStatement[statements.size()]);
        } catch (Exception e) {
            throw new CustomChangeException("Failed to migrate certificate data");
        }

    }

    @Override
    public String getConfirmationMessage() {
        return confirmationMessage;
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
