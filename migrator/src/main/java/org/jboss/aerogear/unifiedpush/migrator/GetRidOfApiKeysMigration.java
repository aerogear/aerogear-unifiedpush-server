package org.jboss.aerogear.unifiedpush.migrator;


import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.CustomChangeException;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class GetRidOfApiKeysMigration implements CustomTaskChange {
    private static class InstallationData {
        private String installationId;
        private String variantId;

        private InstallationData(String installationId, String variantId) {
            this.installationId = installationId;
            this.variantId = variantId;
        }
    }

    @Override
    public void execute(Database database) throws CustomChangeException {
        Connection conn = ((JdbcConnection) (database.getConnection())).getWrappedConnection();
        try {
            conn.setAutoCommit(false);
            List<InstallationData> list = new ArrayList<>();
            String query = "select installation.id as installation_id," +
                    " installation.variant_id as installation_variant_id," +
                    " variant.id as variant_id," +
                    " variant.api_key as variant_api_key" +
                    " from installation join variant on installation.variant_id = variant.api_key";
            PreparedStatement statement = conn.prepareStatement(query);
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                String installationId = rs.getString("installation_id");
                String installationVariantId = rs.getString("installation_variant_id");
                String variantId = rs.getString("variant_id");
                String variantApiKey = rs.getString("variant_api_key");
                list.add(new InstallationData(installationId,variantId));
            }
            String update = "update installation" +
                    " set variant_id = ?" +
                    " where id = ?";
            PreparedStatement updateInstallationsStatement = conn.prepareStatement(update);
            for (InstallationData data: list) {
                updateInstallationsStatement.setString(1, data.variantId);
                updateInstallationsStatement.setString(2, data.installationId);
                updateInstallationsStatement.executeUpdate();
            }

            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
