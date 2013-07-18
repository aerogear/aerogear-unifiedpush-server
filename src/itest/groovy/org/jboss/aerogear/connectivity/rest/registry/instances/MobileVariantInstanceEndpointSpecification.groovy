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
package org.jboss.aerogear.connectivity.rest.registry.instances

import static org.mockito.Matchers.anyString
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.when

import javax.inject.Inject
import javax.servlet.http.HttpServletRequest
import javax.ws.rs.core.Response
import javax.ws.rs.core.UriBuilder
import javax.ws.rs.core.UriInfo
import javax.ws.rs.core.Response.Status

import org.jboss.aerogear.connectivity.api.MobileVariantInstance
import org.jboss.aerogear.connectivity.model.AndroidVariant
import org.jboss.aerogear.connectivity.model.MobileVariantInstanceImpl
import org.jboss.aerogear.connectivity.model.PushApplication
import org.jboss.aerogear.connectivity.rest.registry.applications.AndroidVariantEndpoint
import org.jboss.aerogear.connectivity.rest.registry.applications.PushApplicationEndpoint
import org.jboss.aerogear.connectivity.rest.security.AuthenticationEndpoint
import org.jboss.aerogear.connectivity.service.MobileVariantInstanceService
import org.jboss.aerogear.connectivity.users.Developer
import org.jboss.arquillian.container.test.api.Deployment
import org.jboss.arquillian.spock.ArquillianSpecification
import org.jboss.connectivity.common.Deployments
import org.jboss.shrinkwrap.api.spec.WebArchive
import org.picketlink.common.util.Base64

import spock.lang.Shared
import spock.lang.Specification

@ArquillianSpecification
class MobileVariantInstanceEndpointSpecification extends Specification {

    @Deployment(testable=true)
    def static WebArchive "create deployment"() {
        Deployments.unifiedPushServerWithClasses(MobileVariantInstanceEndpointSpecification.class)
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
    private MobileVariantInstanceEndpoint mobileVariantInstanceEndpoint

    @Inject
    private MobileVariantInstanceService mobileVariantInstanceService

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
        "A Push Application, an Android Variant and a Mobile instance"
        def PushApplication pushApp = buildPushApplication(PUSH_APPLICATION_NAME, PUSH_APPLICATION_DESC)
        def AndroidVariant androidVariant = buildAndroidVariant(ANDROID_VARIANT_GOOGLE_KEY, ANDROID_VARIANT_NAME,
                ANDROID_VARIANT_DESC);
        def ClientInfo clientInfo = new ClientInfo(ANDROID_DEVICE_TOKEN, ANDROID_DEVICE_TYPE, ANDROID_DEVICE_OS,
                ANDROID_DEVICE_OS_VERSION, ANDROID_CLIENT_ALIAS)
        def MobileVariantInstanceImpl mobileVariantInstance = (MobileVariantInstanceImpl) buildMobileVariant(clientInfo)

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
        androidVariantSecret = androidVariant.getSecret()

        and:
        "Registers a Mobile Variant instance"
        def HttpServletRequest mockHttpRequest = mockHttpServletRequestBasicAuth(androidVariantId, androidVariantSecret)
        def mobileVariantRegisterResponse = mobileVariantInstanceEndpoint.registerInstallation(mobileVariantInstance, mockHttpRequest)
        def MobileVariantInstance mobileVariant = (MobileVariantInstance) mobileVariantRegisterResponse.getEntity()

        then:
        "Injections have been performed"
        pushApplicationEndpoint!=null && androidVariantEndpoint != null
        mobileVariantInstanceService != null && mobileVariantInstanceEndpoint != null

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
        List<MobileVariantInstanceImpl> mobileVariantInstanceList  = mobileVariantInstanceService.findMobileVariantInstancesForVariantByToken(
                androidVariantId, mobileVariant.getDeviceToken())
        mobileVariantInstanceList != null && mobileInstanceExistsInList(mobileVariant, mobileVariantInstanceList)
    }

    def "test unregister - removal"() {

        given:
        "A registered mobile variant instance"
        def ClientInfo clientInfo = new ClientInfo(ANDROID_DEVICE_TOKEN, ANDROID_DEVICE_TYPE, ANDROID_DEVICE_OS,
                ANDROID_DEVICE_OS_VERSION, ANDROID_CLIENT_ALIAS)
        def MobileVariantInstanceImpl mobileVariantInstance = (MobileVariantInstanceImpl) buildMobileVariant(clientInfo)

        when:
        "User is logged in"
        login()

        and:
        "Unregisters a mobile variant instance"
        def HttpServletRequest mockHttpRequest = mockHttpServletRequestBasicAuth(androidVariantId, androidVariantSecret)
        def Response response = mobileVariantInstanceEndpoint.unregisterInstallations(mobileVariantInstance.getDeviceToken(),
                mockHttpRequest);

        then:
        "Unregister Mobile Variant instance response status code is 204"
        response != null && response.getStatus() == Status.NO_CONTENT.getStatusCode()

        and:
        "Mobile Variant was indeed unregistered"
        List<MobileVariantInstanceImpl> mobileVariantInstanceList  = mobileVariantInstanceService.findMobileVariantInstancesForVariantByToken(
                androidVariantId, mobileVariantInstance.getDeviceToken())
        !mobileInstanceExistsInList(mobileVariantInstance, mobileVariantInstanceList)
    }

    private boolean mobileInstanceExistsInList(MobileVariantInstance instance,
            List<MobileVariantInstanceImpl> mobileVariantInstanceList) {
        if (instance != null && mobileVariantInstanceList != null) {
            for (MobileVariantInstanceImpl mobileVariantInstance : mobileVariantInstanceList) {
                if (mobileVariantInstance != null && instance.getDeviceToken().equals(mobileVariantInstance.getDeviceToken())) {
                    return true;
                }
            }
        }
        return false;
    }

    private MobileVariantInstance buildMobileVariant(ClientInfo clientInfo) {
        MobileVariantInstance instance = new MobileVariantInstanceImpl();
        instance.setDeviceToken(clientInfo.getToken());
        instance.setDeviceType(clientInfo.getType());
        instance.setMobileOperatingSystem(clientInfo.getOs());
        instance.setOsVersion(clientInfo.getOsVersion());
        instance.setAlias(clientInfo.getAlias());
        return instance;
    }

    private HttpServletRequest mockHttpServletRequestBasicAuth(String mobileVariantID, String secret) {
        String token = new String(Base64.encodeBytes((mobileVariantID + ":" + secret).getBytes()));
        HttpServletRequest mockHttpRequest = mock(HttpServletRequest.class);
        when(mockHttpRequest.getHeader("Authorization")).thenReturn("Basic " + token);
        return mockHttpRequest;
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

    private Developer buildDeveloper(String loginName, String password) {
        Developer developer = new Developer();
        developer.setLoginName(loginName);
        developer.setPassword(password);
        return developer;
    }

    private class ClientInfo {

        private String token;

        private String type;

        private String os;

        private String osVersion;

        private String alias;

        public ClientInfo(String token, String type, String os, String osVersion, String alias) {
            this.setToken(token);
            this.setOs(os);
            this.setType(type);
            this.setOsVersion(osVersion);
            this.setAlias(alias);
        }

        public String getAlias() {
            return alias;
        }

        public void setAlias(String alias) {
            this.alias = alias;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getOs() {
            return os;
        }

        public void setOs(String os) {
            this.os = os;
        }

        public String getOsVersion() {
            return osVersion;
        }

        public void setOsVersion(String osVersion) {
            this.osVersion = osVersion;
        }
    }
}
