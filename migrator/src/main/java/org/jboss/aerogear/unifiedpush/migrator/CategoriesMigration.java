package org.jboss.aerogear.unifiedpush.migrator;

import liquibase.change.custom.CustomSqlChange;
import liquibase.database.Database;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.CustomChangeException;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.InsertStatement;
import liquibase.statement.core.RawSqlStatement;
import liquibase.statement.core.UpdateStatement;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CategoriesMigration implements CustomSqlChange {
    @Override
    public SqlStatement[] generateStatements(Database database) throws CustomChangeException {
        ArrayList<SqlStatement> sqlStatements = new ArrayList<SqlStatement>();
        Connection conn = ((JdbcConnection) (database.getConnection())).getWrappedConnection();

        try {
            ResultSet rs = conn.createStatement().executeQuery("select distinct categories from installation_categories");
            List<String> categories = new ArrayList<String>();
            while (rs.next()) {
                String category = rs.getString(0);
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
                        .addNewColumnValue("nextval", categoryCounter + 1)
                        .addWhereColumnName("nextval")
                        .addWhereParameter(categoryCounter);
                sqlStatements.add(updateStatement);
            } else if ("postgresql".equals(database.getShortName())) {
                RawSqlStatement rawSqlStatement = new RawSqlStatement("alter sequence category_seq start " + categoryCounter + 1);
                sqlStatements.add(rawSqlStatement);
            }

        } catch (SQLException e) {
            throw new CustomChangeException(e);
        }
        return sqlStatements.toArray(new SqlStatement[sqlStatements.size()]);
    }

    @Override
    public String getConfirmationMessage() {
        return null;
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
