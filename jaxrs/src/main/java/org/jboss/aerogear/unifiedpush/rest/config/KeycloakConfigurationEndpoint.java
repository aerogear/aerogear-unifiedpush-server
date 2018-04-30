/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.unifiedpush.rest.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jboss.aerogear.unifiedpush.system.ConfigurationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static org.jboss.aerogear.unifiedpush.rest.util.CommonUtils.removeDefaultHttpPorts;

@Path("/auth/config")
public class KeycloakConfigurationEndpoint {

    private static final String REALM_NAME_PROPERTY = "ups.realm.name";
    private static final String REALM_URL_PROPERTY = "ups.auth.server.url";
    private static final ObjectMapper mapper = new ObjectMapper();

    private static final Logger logger = LoggerFactory.getLogger(KeycloakConfigurationEndpoint.class);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response configurationFile() throws JsonProcessingException {

        final String realmName = ConfigurationUtils.tryGetGlobalProperty(REALM_NAME_PROPERTY);
        final String keycloakServerURL = removeDefaultHttpPorts(ConfigurationUtils.tryGetGlobalProperty(REALM_URL_PROPERTY));

        final Config config = new Config(realmName, keycloakServerURL);

        logger.trace("rendering '{}' realm config, for {}", realmName, keycloakServerURL);

        return Response.ok(mapper.writeValueAsString(config)).build();

    }

    private class Config {

        private String realm = "aerogear";
        @JsonProperty("auth-server-url")
        private String authServerUrl = "/auth";
        @JsonProperty("ssl-required")
        private final String sslRequired = "external";
        @JsonProperty("public-client")
        private final boolean publicClient = true;
        @JsonProperty("auth-enabled")
        private boolean authEnabled = false;
        private final String resource = "unified-push-server-js";

        public Config(String realmName, String authServerUrl) {
            if(realmName != null && !realm.isEmpty()) {
                this.realm = realmName;
            }
            if(authServerUrl != null && !authServerUrl.isEmpty()) {
                logger.trace("UPS is protected by an authentification broker");
                this.authServerUrl = authServerUrl;
                this.authEnabled = true;
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

        public boolean isAuthEnabled() { return authEnabled; }

    }
}
