/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.connectivity.rest.registry.installations

import static org.mockito.Matchers.anyString
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.when

import javax.inject.Inject
import javax.servlet.http.HttpServletRequest
import javax.ws.rs.core.Response
import javax.ws.rs.core.UriBuilder
import javax.ws.rs.core.UriInfo
import javax.ws.rs.core.Response.Status

import org.jboss.aerogear.connectivity.model.AndroidVariant
import org.jboss.aerogear.connectivity.model.InstallationImpl;
import org.jboss.aerogear.connectivity.model.PushApplication
import org.jboss.aerogear.connectivity.rest.registry.applications.AndroidVariantEndpoint
import org.jboss.aerogear.connectivity.rest.registry.applications.PushApplicationEndpoint
import org.jboss.aerogear.connectivity.rest.security.AuthenticationEndpoint
import org.jboss.aerogear.connectivity.service.ClientInstallationService;
import org.jboss.aerogear.connectivity.users.Developer
import org.jboss.arquillian.container.test.api.Deployment
import org.jboss.arquillian.spock.ArquillianSpecification
import org.jboss.aerogear.connectivity.common.AndroidVariantUtils;
import org.jboss.aerogear.connectivity.common.AuthenticationUtils;
import org.jboss.aerogear.connectivity.common.Deployments
import org.jboss.aerogear.connectivity.common.InstallationUtils;
import org.jboss.aerogear.connectivity.common.PushApplicationUtils;
import org.jboss.shrinkwrap.api.spec.WebArchive
import org.picketlink.common.util.Base64

import spock.lang.Shared
import spock.lang.Specification

@ArquillianSpecification
@Mixin([AuthenticationUtils, PushApplicationUtils, AndroidVariantUtils, InstallationUtils])
class InstallationRegistrationEndpointSpecification extends Specification {

    @Deployment(testable=true)
    def static WebArchive "create deployment"() {
        Deployments.unifiedPushServerWithClasses(InstallationRegistrationEndpointSpecification.class, 
            AuthenticationUtils.class, PushApplicationUtils.class, AndroidVariantUtils.class, InstallationUtils.class)
    }

    @Shared private static String pushAppId

    @Shared private static String androidVariantId

    @Shared private static String androidVariantSecret

    @Inject
    private AndroidVariantEndpoint androidVariantEndpoint

    @Inject
    private AuthenticationEndpoint authenticationEndpoint

    @Inject
    private PushApplicationEndpoint pushApplicationEndpoint

    @Inject
    private InstallationRegistrationEndpoint installationRegistrationEndpoint

    @Inject
    private ClientInstallationService clientInstallationService

    private static final String ANDROID_VARIANT_GOOGLE_KEY = "IDDASDASDSA"

    private static final String ANDROID_VARIANT_NAME = "TestAndroidVariant"

    private static final String ANDROID_VARIANT_DESC = "awesome variant"

    private static final String AUTHORIZED_LOGIN_NAME = "admin"

    private static final String AUTHORIZED_PASSWORD = "123"

    private static final String PUSH_APPLICATION_NAME = "TestPushApp"

    private static final String PUSH_APPLICATION_DESC = "awesome app"

    private static final String ANDROID_DEVICE_TOKEN = "someToken"

    private static final String ANDROID_DEVICE_OS = "ANDROID"

    private static final String ANDROID_DEVICE_TYPE = "ANDROID"

    private static final String ANDROID_DEVICE_OS_VERSION = "4.2.2"

    private static final String ANDROID_CLIENT_ALIAS = "qa@mobileteam"

    def "test registration"() {

        given:
        "A Push Application, an Android Variant and an Installation"
        def PushApplication pushApp = createPushApplication(PUSH_APPLICATION_NAME, PUSH_APPLICATION_DESC, "", "", "")
        def AndroidVariant androidVariant = createAndroidVariant(ANDROID_VARIANT_NAME,
                ANDROID_VARIANT_DESC, "", "", "", ANDROID_VARIANT_GOOGLE_KEY);
        def InstallationImpl installationInstance = createInstallation(ANDROID_DEVICE_TOKEN, ANDROID_DEVICE_TYPE, ANDROID_DEVICE_OS,
                ANDROID_DEVICE_OS_VERSION, ANDROID_CLIENT_ALIAS, null)

        when:
        "User is logged in"
        adminLogin()

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
        androidVariantSecret = androidVariant.getSecret()

        and:
        "Registers an installation"
        def HttpServletRequest mockHttpRequest = mockHttpServletRequestBasicAuth(androidVariantId, androidVariantSecret)
        def mobileVariantRegisterResponse = installationRegistrationEndpoint.registerInstallation(installationInstance, mockHttpRequest)
        def InstallationImpl mobileVariant = (InstallationImpl) mobileVariantRegisterResponse.getEntity()

        then:
        "Injections have been performed"
        pushApplicationEndpoint!=null && androidVariantEndpoint != null
        clientInstallationService != null && installationRegistrationEndpoint != null

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
        "Register Mobile Variant instance response status code is 200"
        mobileVariantRegisterResponse != null && mobileVariantRegisterResponse.getStatus() == Status.OK.getStatusCode()

        and:
        "Returned entity is not empty"
        mobileVariant != null

        and:
        "Mobile Variant was indeed registered"
        InstallationImpl instance  = clientInstallationService.findInstallationForVariantByDeviceToken(
                androidVariantId, mobileVariant.getDeviceToken())
        instance != null
    }

    def "test unregister - removal"() {

        given:
        "A registered installation"
        def InstallationImpl installationInstance = createInstallation(ANDROID_DEVICE_TOKEN, ANDROID_DEVICE_TYPE, ANDROID_DEVICE_OS,
                ANDROID_DEVICE_OS_VERSION, ANDROID_CLIENT_ALIAS, "")
        when:
        "User is logged in"
        adminLogin()

        and:
        "Unregisters a mobile variant instance"
        def HttpServletRequest mockHttpRequest = mockHttpServletRequestBasicAuth(androidVariantId, androidVariantSecret)
        def Response response = installationRegistrationEndpoint.unregisterInstallations(installationInstance.getDeviceToken(),
                mockHttpRequest);

        then:
        "Unregister Mobile Variant instance response status code is 204"
        response != null && response.getStatus() == Status.NO_CONTENT.getStatusCode()

        and:
        "Mobile Variant was indeed unregistered"
        def InstallationImpl instance  = clientInstallationService.findInstallationForVariantByDeviceToken(
                androidVariantId, installationInstance.getDeviceToken())
        instance == null
    }

    private HttpServletRequest mockHttpServletRequestBasicAuth(String mobileVariantID, String secret) {
        String token = new String(Base64.encodeBytes((mobileVariantID + ":" + secret).getBytes()));
        HttpServletRequest mockHttpRequest = mock(HttpServletRequest.class);
        when(mockHttpRequest.getHeader("Authorization")).thenReturn("Basic " + token);
        return mockHttpRequest;
    }

    def adminLogin() {
        // test default login
        def developer = createDeveloper(AUTHORIZED_LOGIN_NAME, AUTHORIZED_PASSWORD);
        Response response = authenticationEndpoint.login(developer);
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

}
