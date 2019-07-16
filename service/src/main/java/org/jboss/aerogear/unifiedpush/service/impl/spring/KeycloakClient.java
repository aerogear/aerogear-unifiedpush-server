package org.jboss.aerogear.unifiedpush.service.impl.spring;

import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Lazy
@Component
class KeycloakClient {
    private final Logger logger = LoggerFactory.getLogger(KeycloakClient.class);

    @Autowired
    private IOAuth2Configuration conf;

    private Keycloak keycloak;

    @PostConstruct
    public void init(){
        String keycloakPath = conf.getOAuth2Url();
        String upsMasterRealmName = conf.getUpsMasterRealm();
        String cliClientId = conf.getAdminClient();
        String userName = conf.getAdminUserName();
        String userPassword = conf.getMasterPassword();

        keycloak = KeycloakBuilder.builder() //
                .serverUrl(keycloakPath) //
                .realm(upsMasterRealmName)//
                .username(userName) //
                .password(userPassword) //
                .clientId(cliClientId) //
                .resteasyClient( //
                        // Setting TTL to 10 seconds, prevent KC token
                        // expiration.
                        new ResteasyClientBuilder().connectionPoolSize(25).connectionTTL(10, TimeUnit.SECONDS).build()) //
                .build();
        logger.info("KeycloakClient Initialized");
    }

    public Keycloak getKeycloak(){
        return keycloak;
    }
}
