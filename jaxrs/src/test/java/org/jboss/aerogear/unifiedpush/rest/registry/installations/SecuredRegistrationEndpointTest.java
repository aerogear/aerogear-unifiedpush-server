package org.jboss.aerogear.unifiedpush.rest.registry.installations;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.aerogear.unifiedpush.api.Alias;
import org.jboss.aerogear.unifiedpush.api.PushApplication;
import org.jboss.aerogear.unifiedpush.cassandra.dao.AliasDao;
import org.jboss.aerogear.unifiedpush.rest.RestEndpointTest;
import org.jboss.aerogear.unifiedpush.rest.WebConfigTest;
import org.jboss.aerogear.unifiedpush.rest.util.Authenticator;
import org.jboss.aerogear.unifiedpush.service.AliasService;
import org.jboss.aerogear.unifiedpush.service.DocumentService;
import org.jboss.aerogear.unifiedpush.service.PushApplicationService;
import org.jboss.aerogear.unifiedpush.service.annotations.LoggedInUser;
import org.jboss.aerogear.unifiedpush.service.impl.UserTenantInfo;
import org.jboss.aerogear.unifiedpush.service.impl.spring.IKeycloakService;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.datastax.driver.core.utils.UUIDs;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = { WebConfigTest.class })
public class SecuredRegistrationEndpointTest extends RestEndpointTest {

    @Inject
    private IKeycloakService keycloakService;
    @Inject
    private PushApplicationService pushApplicationService;
    @Inject
    private DocumentService documentService;
    @Inject
    AliasService aliasService;
    @Inject
    private PushApplicationService pushAppService;
    @Inject
    private AliasDao aliasDao;

    /** The flow:
     * create realm and corresponding client
     * create user at DB
     * create same user at Keycloak without utr
     * sync UTR
     * validate keycloak UTR created and matches DB UTR
     */
    @Test
    public void syncUtrPositiveTest() {
       String realmName = "test-app";
        //create realm "unifiedpush-installations" if needed
        keycloakService.createRealmIfAbsent(realmName);

        // Create client assuming realm "unifiedpush-installations" already exists
        final String appName = "test-app";
        String userName = "mike@mail.com";
        PushApplication app = new PushApplication();
        app.setName(appName);
        pushAppService.addPushApplication(app, new LoggedInUser(userName));
        keycloakService.createClientIfAbsent(app);
        keycloakService.setDirectAccessGrantsEnabled(appName, realmName, true);

        //create user at keycloak
        String password = "password";
        keycloakService.delete(userName, appName);
        keycloakService.createVerifiedUserIfAbsent(userName, password, Collections.emptyList(), realmName);
        keycloakService.setPasswordUpdateRequired(userName, realmName, false);
        keycloakService.updateUserPassword(userName, password, password, appName);

        //create same user at DB
        UUID id = UUIDs.timeBased();
        String email = userName;
        String other = "";

        Alias alias = new Alias(UUID.fromString(app.getPushApplicationID()), id, email, other);
        aliasDao.create(alias);

        //invoke utrSync endpoint
        String token = keycloakService.getUserAccessToken(userName, password, realmName, appName);
        ResteasyClient client = new ResteasyClientBuilder().register(new Authenticator(app.getPushApplicationID(), app.getMasterSecret())).build();
        ResteasyWebTarget target = client.target(getRestFullPath() + "/registry/type/syncUtr/" + alias.getEmail().toLowerCase());
        Response response = target.request().header(HttpHeaders.AUTHORIZATION, "bearer " + token).post(Entity.entity(app, MediaType.APPLICATION_JSON_TYPE));
        if (response.getStatus() != Response.Status.OK.getStatusCode()) {
            Assert.fail("Failed to update UTR. Got status code " + response.getStatus());
        }

        //validate utr added to the keycloak user
        List<String> utrFromKeycloak = keycloakService.getUtr(userName, realmName);
        UserTenantInfo utrFromDb = aliasService.getTenantRelations(userName).iterator().next();
        if (utrFromKeycloak.size() != 1) {
            Assert.fail("Got invalid UTR from keycloak user " + userName + "  with size " + utrFromKeycloak.size());
        }

        if (utrFromDb == null) {
            Assert.fail("Got invalid UTR from DB user " + userName + " with size " + utrFromKeycloak.size());
        }

        UserTenantInfo parsedUtr = null;
        try {
            JSONObject json = (JSONObject) new JSONParser().parse(utrFromKeycloak.get(0));
            parsedUtr = new UserTenantInfo(UUID.fromString(json.get("uid").toString()), UUID.fromString(json.get("pid").toString()), json.get("cid").toString());
        } catch (ParseException e) {
            Assert.fail("Failed to parse UTR from keycloak");
        }

        Assert.assertEquals("syncUtrPositiveTest", parsedUtr, utrFromDb);
    }

    @Test
    public void syncUtrNegativeTest() {
        String realmName = "test-app";
        //create realm "unifiedpush-installations" if needed
        keycloakService.createRealmIfAbsent(realmName);

        // Create client assuming realm "unifiedpush-installations" already exists
        final String appName = "test-app";
        String userName = "mike@mail.com";
        PushApplication app = new PushApplication();
        app.setName(appName);
        pushAppService.addPushApplication(app, new LoggedInUser(userName));
        keycloakService.createClientIfAbsent(app);
        keycloakService.setDirectAccessGrantsEnabled(appName, realmName, true);

        //create user at keycloak
        String password = "password";
        keycloakService.delete(userName, appName);
        keycloakService.createVerifiedUserIfAbsent(userName, password, Collections.emptyList(), realmName);
        keycloakService.setPasswordUpdateRequired(userName, realmName, false);
        keycloakService.updateUserPassword(userName, password, password, appName);

        //create same user at DB
        UUID id = UUIDs.timeBased();
        String email = userName;
        String other = "";

        Alias alias = new Alias(UUID.fromString(app.getPushApplicationID()), id, email, other);
        aliasDao.create(alias);

        //invoke utrSync endpoint
        String token = keycloakService.getUserAccessToken(userName, password, realmName, appName);
        ResteasyClient client = new ResteasyClientBuilder().register(new Authenticator(app.getPushApplicationID(), app.getMasterSecret())).build();
        ResteasyWebTarget target = client.target(getRestFullPath() + "/registry/type/syncUtr/" + "WRONG_" + alias.getEmail().toLowerCase());
        Response response = target.request().header(HttpHeaders.AUTHORIZATION, "bearer " + token).post(Entity.entity(app, MediaType.APPLICATION_JSON_TYPE));
        Assert.assertEquals("syncUtrNegativeTest", response.getStatus(), Response.Status.NOT_FOUND.getStatusCode());
    }
}
