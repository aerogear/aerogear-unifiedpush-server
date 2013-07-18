package org.jboss.aerogear.connectivity.rest.registry.applications

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Set;

import javax.ejb.EJBException;
import javax.inject.Inject;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang3.StringUtils;
import org.jboss.aerogear.connectivity.model.AndroidVariant;
import org.jboss.aerogear.connectivity.model.PushApplication;
import org.jboss.aerogear.connectivity.rest.security.AuthenticationEndpoint;
import org.jboss.aerogear.connectivity.service.AndroidVariantService;
import org.jboss.aerogear.connectivity.users.Developer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.spock.ArquillianSpecification;
import org.jboss.connectivity.common.Deployments;
import org.jboss.resteasy.spi.UnauthorizedException;
import org.jboss.shrinkwrap.api.spec.WebArchive;

import spock.lang.Shared;
import spock.lang.Specification;

@ArquillianSpecification
class AndroidVariantEndpointSpecification extends Specification {

    @Deployment(testable=true)
    def static WebArchive "create deployment"() {
        Deployments.unifiedPushServerWithClasses(AndroidVariantEndpointSpecification.class)
    }

    @Shared private static String pushAppId

    @Shared private static String androidVariantId

    @Inject
    private AndroidVariantEndpoint androidVariantEndpoint

    @Inject
    private AuthenticationEndpoint authenticationEndpoint

    @Inject
    private AndroidVariantService androidVariantService

    @Inject
    private PushApplicationEndpoint pushApplicationEndpoint

    private static final String ANDROID_VARIANT_GOOGLE_KEY = "IDDASDASDSAQ"

    private static final String ANDROID_VARIANT_NAME = "TestAndroidVariant1"

    private static final String ANDROID_VARIANT_DESC = "awesome variant1"

    private static final String UPDATED_ANDROID_VARIANT_GOOGLE_KEY = "UPD-IDDASDASDSAQ"

    private static final String UPDATED_ANDROID_VARIANT_NAME = "UPD-TestAndroidVariant1"

    private static final String UPDATED_ANDROID_VARIANT_DESC = "UPD-awesome variant1"

    private static final String AUTHORIZED_LOGIN_NAME = "admin"

    private static final String AUTHORIZED_PASSWORD = "123"

    private static final String PUSH_APPLICATION_NAME = "TestPushApp1"

    private static final String PUSH_APPLICATION_DESC = "awesome app1"

    private static final String NOT_EXISTING_PUSH_ID = "1234567890a1"

    private static final String NOT_EXISTING_ANDROID_ID = "1234567890b1"

    def "test unauthorized registration"() {

        when:
        "Registering an Android variant without being authorized"
        androidVariantEndpoint.registerAndroidVariant(null, null, null)

        then:
        "androidVariantEndpoint was injected"
        androidVariantEndpoint!=null

        and:
        "EJBException with an UnauthorizedException cause is thrown"
        def ex = thrown(EJBException)
        ex.cause instanceof UnauthorizedException
    }

    def "test unauthorized listing"() {

        when:
        "Listing all Android variants without being authorized"
        androidVariantEndpoint.listAllAndroidVariationsForPushApp(null)

        then:
        "androidVariantEndpoint was injected"
        androidVariantEndpoint!=null

        and:
        "EJBException with an UnauthorizedException cause is thrown"
        def ex = thrown(EJBException)
        ex.cause instanceof UnauthorizedException
    }

    def "test unauthorized find by id"() {

        when:
        "Finding an Android variant by id without being authorized"
        androidVariantEndpoint.findAndroidVariationById(null, null)

        then:
        "androidVariantEndpoint was injected"
        androidVariantEndpoint!=null

        and:
        "EJBException with an UnauthorizedException cause is thrown"
        def ex = thrown(EJBException)
        ex.cause instanceof UnauthorizedException
    }

    def "test unauthorized update"() {

        when:
        "Updating an Android variant without being authorized"
        androidVariantEndpoint.updateAndroidVariation(null, null, null)

        then:
        "androidVariantEndpoint was injected"
        androidVariantEndpoint!=null

        and:
        "EJBException with an UnauthorizedException cause is thrown"
        def ex = thrown(EJBException)
        ex.cause instanceof UnauthorizedException
    }

    def "test unauthorized deletion"() {

        when:
        "Deleting an Android variant without being authorized"
        androidVariantEndpoint.deleteAndroidVariation(null, null)

        then:
        "androidVariantEndpoint was injected"
        androidVariantEndpoint!=null

        and:
        "EJBException with an UnauthorizedException cause is thrown"
        def ex = thrown(EJBException)
        ex.cause instanceof UnauthorizedException
    }

    def "test registration"() {

        given:
        "A Push Application"
        def PushApplication pushApp = buildPushApplication(PUSH_APPLICATION_NAME, PUSH_APPLICATION_DESC)
        def AndroidVariant androidVariant = buildAndroidVariant(ANDROID_VARIANT_GOOGLE_KEY, ANDROID_VARIANT_NAME,
                ANDROID_VARIANT_DESC);

        when:
        "User is logged in"
        login()

        and:
        "Registers the push application"
        def Response registerPushAppResponse = pushApplicationEndpoint.registerPushApplication(pushApp)
        pushApp = (PushApplication)registerPushAppResponse.getEntity()
        pushAppId = pushApp.getPushApplicationID()

        and:
        "Registers an Android variant"
        def Response registerAndroidVariantResponse = androidVariantEndpoint.registerAndroidVariant(androidVariant, pushApp.getPushApplicationID(),
                mockUriInfo("http://domain:port/ag-push/rest/applications/" + ANDROID_VARIANT_GOOGLE_KEY + "/android"))
        androidVariant = (AndroidVariant) registerAndroidVariantResponse.getEntity()
        androidVariantId = androidVariant.getVariantID()

        then:
        "Injections have been performed"
        pushApplicationEndpoint!=null && androidVariantEndpoint != null && androidVariantService != null

        and:
        "Register Push App response status code is 201"
        registerPushAppResponse != null && registerPushAppResponse.getStatus() == Status.CREATED.getStatusCode()

        and:
        "Register Push App response contains the push application id"
        pushAppId != null

        and:
        "Register Android variant response status code is 201"
        registerAndroidVariantResponse != null && registerAndroidVariantResponse.getStatus() == Status.CREATED.getStatusCode()

        and:
        "Register Android variant response contains the variant id"
        androidVariantId != null

        and:
        "The Android Variant was indeed registered"
        def AndroidVariant findAndroidVariant = androidVariantService.findByVariantIDForDeveloper(androidVariant.getVariantID(),
                AUTHORIZED_LOGIN_NAME)
        findAndroidVariant != null
    }

    def "test listing"() {

        when:
        "User is logged in"
        login()

        and:
        "Lists all the Android variations for push app id"
        def listAndroidVariationsResponse = androidVariantEndpoint.listAllAndroidVariationsForPushApp(pushAppId)

        then:
        "Injections have been performed"
        androidVariantEndpoint!=null

        and:
        "Push App id & Android Variant Id exist"
        pushAppId != null && androidVariantId != null

        and:
        "Listing Response status code is 200"
        listAndroidVariationsResponse != null && listAndroidVariationsResponse.getStatus() == Status.OK.getStatusCode()

        and:
        "Listing response contains entity"
        listAndroidVariationsResponse.getEntity() != null

        and:
        "Android Variant id exists in list"
        def Set<AndroidVariant> androidVariantSet = (Set<AndroidVariant>) listAndroidVariationsResponse.getEntity()
        androidVariantSet != null && appIdExistsInSet(androidVariantId, androidVariantSet)
    }

    def "test find by id"() {

        when:
        "User is logged in"
        login()

        and:
        "Searches for a registered Android Variant by id"
        def findByIdResponse = androidVariantEndpoint.findAndroidVariationById(pushAppId,
                androidVariantId)

        then:
        "Injections have been performed"
        androidVariantEndpoint!=null

        and:
        "Push App id & Android Variant Id exist"
        pushAppId != null && androidVariantId != null

        and:
        "Find by id response status code is 200"
        findByIdResponse != null && findByIdResponse.getStatus() == Status.OK.getStatusCode()

        and:
        "Find by id response contains entity"
        findByIdResponse.getEntity() != null

        and:
        "Push application in response has the correct id"
        def AndroidVariant androidVariant = (AndroidVariant) findByIdResponse.getEntity()
        androidVariant != null && androidVariantId.equals(androidVariant.getVariantID())
    }

    def "test update"() {

        given:
        "Updated Android Variant"
        def AndroidVariant updatedAndroidvariant = buildAndroidVariant(UPDATED_ANDROID_VARIANT_GOOGLE_KEY,
                UPDATED_ANDROID_VARIANT_NAME, UPDATED_ANDROID_VARIANT_DESC)

        when:
        "User is logged in"
        login()

        and:
        "Updates the registered Android Variant"
        def updateResponse = androidVariantEndpoint.updateAndroidVariation(pushAppId,
                androidVariantId, updatedAndroidvariant)

        "Searches the Android Variant by id"
        def findByIdResponse = androidVariantEndpoint.findAndroidVariationById(pushAppId,
                androidVariantId)

        then:
        "Injections have been performed"
        androidVariantEndpoint!=null && androidVariantService != null

        and:
        "Push App id & Android Variant id exist"
        pushAppId != null && androidVariantId != null

        and:
        "Update response status code is 204"
        updateResponse != null && updateResponse.getStatus() == Status.NO_CONTENT.getStatusCode()

        and:
        "Find by id response status code is 200"
        findByIdResponse != null && findByIdResponse.getStatus() == Status.OK.getStatusCode()

        and:
        "Find by id response contains entity"
        findByIdResponse.getEntity() != null

        and:
        "Android Variant in response has the updated details"
        def AndroidVariant androidvariant = (AndroidVariant) findByIdResponse.getEntity()
        androidvariant != null && UPDATED_ANDROID_VARIANT_DESC.equals(androidvariant.getDescription())
        UPDATED_ANDROID_VARIANT_NAME.equals(androidvariant.getName()) && UPDATED_ANDROID_VARIANT_GOOGLE_KEY.equals(androidvariant.getGoogleKey())

        and:
        "Android Variant was updated on the underlying service"
        def AndroidVariant findAndroidVariant = androidVariantService.findByVariantIDForDeveloper(androidVariantId, AUTHORIZED_LOGIN_NAME)
        findAndroidVariant != null && UPDATED_ANDROID_VARIANT_DESC.equals(findAndroidVariant.getDescription())
        UPDATED_ANDROID_VARIANT_NAME.equals(findAndroidVariant.getName()) && UPDATED_ANDROID_VARIANT_GOOGLE_KEY.equals(findAndroidVariant.getGoogleKey())
    }

    def "test deletion"() {

        when:
        "User is logged in"
        login()

        and:
        "Deletes and Android variant"
        def deleteResponse = androidVariantEndpoint.deleteAndroidVariation(pushAppId,
                    androidVariantId)

        "Searches for the deleted Android variant"
        def findByIdResponse = androidVariantEndpoint.findAndroidVariationById(pushAppId,
            androidVariantId)
        
        then:
        "Injections have been performed"
        pushApplicationEndpoint!=null && androidVariantEndpoint != null && androidVariantService != null

        and:
        "Push App id & Android Variant id exist"
        pushAppId != null && androidVariantId != null

        and:
        "Delete response status code is 204"
        deleteResponse != null && deleteResponse.getStatus() == Status.NO_CONTENT.getStatusCode()

        and:
        "Find by id response status code is 404"
        findByIdResponse != null && findByIdResponse.getStatus() == Status.NOT_FOUND.getStatusCode()

        and:
        "Deleted push application does not exist"
        def AndroidVariant findAndroidVariant = androidVariantService.findByVariantIDForDeveloper(androidVariantId, AUTHORIZED_LOGIN_NAME)
        findAndroidVariant == null
    }

    private void login() {
        // test default login
        Developer developer = buildDeveloper(AUTHORIZED_LOGIN_NAME, AUTHORIZED_PASSWORD);
        Response response = authenticationEndpoint.login(developer);
    }

    private PushApplication buildPushApplication(String name, String description) {
        PushApplication pushApp = new PushApplication();
        pushApp.setName(name);
        pushApp.setDescription(description);
        return pushApp;
    }

    private AndroidVariant buildAndroidVariant(String googleKey, String name, String description) {
        AndroidVariant androidVariant = new AndroidVariant();
        androidVariant.setGoogleKey(googleKey);
        androidVariant.setName(name);
        androidVariant.setDescription(description);
        return androidVariant;
    }

    private UriInfo mockUriInfo(String absolutePath) {
        UriInfo uriInfo = mock(UriInfo.class);
        UriBuilder uriBuilder = mock(UriBuilder.class);
        try {
            when(uriBuilder.path(anyString())).thenReturn(uriBuilder);
            when(uriBuilder.path(anyString()).build()).thenReturn(new URI(absolutePath));
            when(uriInfo.getAbsolutePath()).thenReturn(new URI(absolutePath));
            when(uriInfo.getAbsolutePathBuilder()).thenReturn(uriBuilder);
        } catch (URISyntaxException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return uriInfo;
    }

    private boolean appIdExistsInSet(String androidVariantId, Set<AndroidVariant> androidVariantSet) {
        if (!StringUtils.isEmpty(androidVariantId) && androidVariantSet != null) {
            for (AndroidVariant androidVariantApp : androidVariantSet) {
                if (androidVariantApp != null && androidVariantId.equals(androidVariantApp.getVariantID())) {
                    return true;
                }
            }
        }
        return false;
    }

    private Developer buildDeveloper(String loginName, String password) {
        Developer developer = new Developer();
        developer.setLoginName(loginName);
        developer.setPassword(password);
        return developer;
    }
}
