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
package org.jboss.aerogear.connectivity.rest.registry.applications

import javax.ejb.EJBException;
import javax.inject.Inject;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang3.StringUtils;
import org.jboss.aerogear.connectivity.model.PushApplication;
import org.jboss.aerogear.connectivity.rest.security.AuthenticationEndpoint;
import org.jboss.aerogear.connectivity.service.PushApplicationService;
import org.jboss.aerogear.connectivity.users.Developer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.spock.ArquillianSpecification;
import org.jboss.connectivity.common.Deployments;
import org.jboss.resteasy.spi.UnauthorizedException;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.archive.importer.MavenImporter;

import spock.lang.Shared;
import spock.lang.Specification;

@ArquillianSpecification
class PushApplicationEndpointSpecification extends Specification {

    @Inject
    private PushApplicationEndpoint pushApplicationEndpoint

    @Inject
    private AuthenticationEndpoint authenticationEndpoint

    @Inject
    private PushApplicationService pushAppService

    @Shared private static String pushAppId

    private static final String PUSH_APPLICATION_NAME = "TestApp"

    private static final String PUSH_APPLICATION_DESC = "awesome app"

    private static final String PUSH_APPLICATION_UPDATED_NAME = "TestAppUpdated"

    private static final String PUSH_APPLICATION_UPDATED_DESC = "awesome app updated"

    private static final String AUTHORIZED_LOGIN_NAME = "admin"

    private static final String AUTHORIZED_PASSWORD = "123"

    private static final String NOT_EXISTING_PUSH_ID = "1234567890a"

    @Deployment(testable=true)
    def static WebArchive "create deployment"() {
        Deployments.unifiedPushServerWithClasses(PushApplicationEndpointSpecification.class)
    }

    def "test unauthorized registration"() {

        when:
        "Registering a Push application without being authorized"
        pushApplicationEndpoint.registerPushApplication(null)

        then:
        "pushApplicationEndpoint was injected"
        pushApplicationEndpoint!=null

        and:
        "EJBException with an UnauthorizedException cause is thrown"
        def ex = thrown(EJBException)
        ex.cause instanceof UnauthorizedException
    }

    def "test unauthorized listing"() {

        when:
        "Listing all Push applications without being authorized"
        pushApplicationEndpoint.listAllPushApplications()

        then:
        "pushApplicationEndpoint was injected"
        pushApplicationEndpoint!=null

        and:
        "EJBException with an UnauthorizedException cause is thrown"
        def ex = thrown(EJBException)
        ex.cause instanceof UnauthorizedException
    }

    def "test unauthorized find by id"() {

        when:
        "Finding a Push application by id without being authorized"
        pushApplicationEndpoint.findById(null)

        then:
        "pushApplicationEndpoint was injected"
        pushApplicationEndpoint!=null

        and:
        "EJBException with an UnauthorizedException cause is thrown"
        def ex = thrown(EJBException)
        ex.cause instanceof UnauthorizedException
    }

    def "test unauthorized update"() {

        when:
        "Updating a Push application without being authorized"
        pushApplicationEndpoint.updatePushApplication(null, null)

        then:
        "pushApplicationEndpoint was injected"
        pushApplicationEndpoint!=null

        and:
        "EJBException with an UnauthorizedException cause is thrown"
        def ex = thrown(EJBException)
        ex.cause instanceof UnauthorizedException
    }

    def "test unauthorized deletion"() {

        when:
        "Deleting a Push application without being authorized"
        pushApplicationEndpoint.deletePushApplication(null)

        then:
        "pushApplicationEndpoint was injected"
        pushApplicationEndpoint!=null

        and:
        "EJBException with an UnauthorizedException cause is thrown"
        def ex = thrown(EJBException)
        ex.cause instanceof UnauthorizedException
    }

    def "test registration"() {

        given:
        "A Push Application"
        def PushApplication pushApp = buildPushApplication(PUSH_APPLICATION_NAME, PUSH_APPLICATION_DESC)

        when:
        "User is logged in"
        login()

        and:
        "Registers the push application"
        def Response registerResponse = pushApplicationEndpoint.registerPushApplication(pushApp)
        pushApp = (PushApplication)registerResponse.getEntity()
        pushAppId = pushApp.getPushApplicationID()

        then:
        "Injections have been performed"
        pushApplicationEndpoint!=null && pushAppService != null

        and:
        "Register response status code is 201"
        registerResponse != null && registerResponse.getStatus() == Status.CREATED.getStatusCode()

        and:
        "Register response entity contains the push application id"
        pushAppId != null

        and:
        "The application was indeed registered"
        def PushApplication findPushApp = pushAppService.findByPushApplicationID(pushAppId)
        findPushApp != null
    }

    def "test listing"() {

        when:
        "User is logged in"
        login()

        and:
        "Lists all the registered push applications"
        def listPushAppResponse = pushApplicationEndpoint.listAllPushApplications()

        then:
        "Injections have been performed"
        pushApplicationEndpoint!=null

        and:
        "Push App id exists"
        pushAppId != null

        and:
        "Listing Response status code is 200"
        listPushAppResponse != null && listPushAppResponse.getStatus() == Status.OK.getStatusCode()

        and:
        "Listing response contains entity"
        listPushAppResponse.getEntity() != null

        and:
        "Push application id exists in list"
        def List<PushApplication> pushAppsList = (List<PushApplication>) listPushAppResponse.getEntity()
        pushAppsList != null && appIdExistsInList(pushAppId, pushAppsList)
    }

    def "test find by id"() {

        when:
        "User is logged in"
        login()

        and:
        "Searches for a registered a push application by id"
        def findByIdResponse = pushApplicationEndpoint.findById(pushAppId)

        then:
        "Injections have been performed"
        pushApplicationEndpoint!=null

        and:
        "Push App id exists"
        pushAppId != null

        and:
        "Find by id response status code is 200"
        findByIdResponse != null && findByIdResponse.getStatus() == Status.OK.getStatusCode()

        and:
        "Find by id response contains entity"
        findByIdResponse.getEntity() != null

        and:
        "Push application in response has the correct id"
        def PushApplication pushApplication = (PushApplication) findByIdResponse.getEntity()
        pushApplication != null && pushAppId.equals(pushApplication.getPushApplicationID())
    }

    def "test update"() {

        given:
        "Updated push application"
        def PushApplication updatedPushApp = buildPushApplication(PUSH_APPLICATION_UPDATED_NAME, PUSH_APPLICATION_UPDATED_DESC)

        when:
        "User is logged in"
        login()

        and:
        "Updates a registered push application"
        def updateResponse = pushApplicationEndpoint.updatePushApplication(pushAppId, updatedPushApp)

        "Seraches for the push application by id"
        def findByIdResponse = pushApplicationEndpoint.findById(pushAppId)

        then:
        "Injections have been performed"
        pushApplicationEndpoint!=null && pushAppService != null

        and:
        "Push App id exists"
        pushAppId != null

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
        "Push application in response has the updated details"
        def PushApplication pushApplication = (PushApplication) findByIdResponse.getEntity()
        pushApplication != null && PUSH_APPLICATION_UPDATED_DESC.equals(pushApplication.getDescription()) && PUSH_APPLICATION_UPDATED_NAME.equals(pushApplication.getName())

        and:
        "Push application was updated on the underlying service"
        def PushApplication foundPushApp = pushAppService.findByPushApplicationID(pushAppId)
        foundPushApp != null && PUSH_APPLICATION_UPDATED_DESC.equals(foundPushApp.getDescription()) && PUSH_APPLICATION_UPDATED_NAME.equals(foundPushApp.getName())
    }

    def "test deletion"() {

        when:
        "User is logged in"
        login()

        and:
        "Deletes push application by id"
        def deleteResponse = pushApplicationEndpoint.deletePushApplication(pushAppId)

        "Searches for a push application by id"
        def findByIdResponse = pushApplicationEndpoint.findById(pushAppId)
        
        then:
        "Injections have been performed"
        pushApplicationEndpoint!=null

        and:
        "Push App id exists"
        pushAppId != null

        and:
        "Delete response status code is 204"
        deleteResponse != null && deleteResponse.getStatus() == Status.NO_CONTENT.getStatusCode()

        and:
        "Find by id response status code is 404"
        findByIdResponse != null && findByIdResponse.getStatus() == Status.NOT_FOUND.getStatusCode()

        and:
        "Deleted push application does not exist"
        def PushApplication foundPushApp = pushAppService.findByPushApplicationID(pushAppId)
        foundPushApp == null
    }

    private void login() {
        Developer developer = buildDeveloper(AUTHORIZED_LOGIN_NAME, AUTHORIZED_PASSWORD)
        Response response = authenticationEndpoint.login(developer)
    }

    private boolean appIdExistsInList(String pushAppId, List<PushApplication> pushAppsList) {
        if (!StringUtils.isEmpty(pushAppId) && pushAppsList != null) {
            for (PushApplication pushApp : pushAppsList) {
                if (pushApp != null && pushAppId.equals(pushApp.getPushApplicationID())) {
                    return true
                }
            }
        }
        return false
    }

    private Developer buildDeveloper(String loginName, String password) {
        Developer developer = new Developer()
        developer.setLoginName(loginName)
        developer.setPassword(password)
        return developer
    }

    private PushApplication buildPushApplication(String name, String description) {
        PushApplication pushApp = new PushApplication()
        pushApp.setName(name)
        pushApp.setDescription(description)
        return pushApp
    }
}
