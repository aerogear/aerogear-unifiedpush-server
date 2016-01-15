package org.jboss.aerogear.unifiedpush.rest.config;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.logging.Logger;

@Path("/keycloak/config")
public class KeycloakConfigurationEndpoint {

    private static final Logger LOGGER = Logger.getLogger(KeycloakConfigurationEndpoint.class.getSimpleName());

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response configurationFile() {

        String realmName = System.getProperty("ups.realm.name");
        String upsAuthServer = System.getProperty("ups.auth.server.url");

        Config config = new Config(realmName, upsAuthServer);

        return Response.ok(new Gson().toJson(config)).build();

    }

    private class Config {

        private String realm = "aerogear";
        @SerializedName("auth-server-url")
        private String authServerUrl = "/auth";
        @SerializedName("ssl-required")
        private final String sslRequired = "external";
        @SerializedName("public-client")
        private final boolean publicClient = true;
        private final String resource = "unified-push-server-js";

        public Config(String realmName, String authServerUrl) {
            if(realmName != null && !realm.isEmpty()) {
                this.realm = realmName;
            }
            if(authServerUrl != null && !authServerUrl.isEmpty()) {
                this.authServerUrl = authServerUrl;
            }
        }

        public String getRealm() {
            return realm;
        }

        public String getAuthServerUrl() {
            return authServerUrl;
        }

        public String getSslRequired() {
            return sslRequired;
        }

        public String getResource() {
            return resource;
        }

        public boolean isPublicClient() {
            return publicClient;
        }

    }
}
