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
package org.jboss.aerogear.connectivity.rest.security

import java.util.List;

import javax.ejb.EJBException;
import javax.inject.Inject
import javax.ws.rs.core.Response
import javax.ws.rs.core.Response.Status

import org.jboss.aerogear.connectivity.jpa.dao.impl.PushDaoSpecification;
import org.jboss.aerogear.connectivity.users.Developer
import org.jboss.aerogear.security.auth.AuthenticationManager
import org.jboss.aerogear.security.authz.IdentityManagement
import org.jboss.arquillian.container.test.api.Deployment
import org.jboss.arquillian.spock.ArquillianSpecification
import org.jboss.connectivity.common.Deployments
import org.jboss.resteasy.spi.UnauthorizedException;
import org.jboss.shrinkwrap.api.Filters;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.archive.importer.MavenImporter;
import org.picketlink.authentication.UserAlreadyLoggedInException;
import org.picketlink.idm.model.SimpleUser;

import spock.lang.Specification

@ArquillianSpecification
class AuthenticationEndpointSpecification extends Specification {

    @Inject
    private AuthenticationEndpoint authenticationEndpoint

    @Inject
    private IdentityManagement identityManagement

    @Inject
    private AuthenticationManager authenticationManager

    private static final String AUTHORIZED_LOGIN_NAME = "admin"

    private static final String AUTHORIZED_PASSWORD = "123"

    private static final String ENROLL_LOGIN_NAME = "newAdmin"

    private static final String ENROLL_PASSWORD = "123"

    private static final String UNAUTHORIZED_LOGIN_NAME = "admin_1"

    private static final String UNAUTHORIZED_PASSWORD = "1234"

    @Deployment(testable=true)
    def static WebArchive "create deployment"() {
        Deployments.unifiedPushServerWithClasses(AuthenticationEndpointSpecification.class);
    }

    def "verify admin login"() {
        given:
        "Admin developer"
        def developer = buildDeveloper(AUTHORIZED_LOGIN_NAME, AUTHORIZED_PASSWORD)

        when:
        "Performing Login"
        def response = authenticationEndpoint.login(developer)

        and:
        "Trying to login while being logged in"
        authenticationManager.login(developer, developer.getPassword())

        then:
        "AuthenticationEndpoint was injected"
        authenticationEndpoint!=null

        and:
        "Response status is 200"
        response != null && response.getStatus() == Status.OK.getStatusCode()

        and:
        "UserAlreadyLoggedInException occurs"
        thrown(UserAlreadyLoggedInException)
    }

    def "test authorized enroll endpoint"() {
        given:
        "Developers"
        def admin = buildDeveloper(AUTHORIZED_LOGIN_NAME, AUTHORIZED_PASSWORD)
        def developer = developer = buildDeveloper(ENROLL_LOGIN_NAME, ENROLL_PASSWORD)

        when:
        "Performing Login"
        def loginResponse = authenticationEndpoint.login(admin)

        and:
        "Enroll the developer"
        def enrollResponse = authenticationEndpoint.enroll(developer)

        and:
        "Retrieving the previously created User from the underlying service"
        def newUser = (SimpleUser) identityManagement.findByUsername(ENROLL_LOGIN_NAME)

        and:
        "Retrieving Users in role"
        def usersInRole = identityManagement.findAllByRole("developer")

        then:
        "Injections have been performed"
        authenticationEndpoint!=null && identityManagement != null

        and:
        "Login response status is 200"
        loginResponse != null && loginResponse.getStatus() == Status.OK.getStatusCode()

        and:
        "Enroll response status is 200"
        enrollResponse != null && enrollResponse.getStatus() == Status.OK.getStatusCode()

        and:
        "The User has been created"
        newUser != null && ENROLL_LOGIN_NAME.equals(newUser.getLoginName())

        and:
        "The user has the right role"
        usersInRole != null && usersInRole.contains(newUser)
    }

    def "test login using wrong credentials"() {
        given:
        "Non existing developer"
        def developer = buildDeveloper(UNAUTHORIZED_LOGIN_NAME, UNAUTHORIZED_PASSWORD)

        when:
        "Performing Login"
        def response = authenticationEndpoint.login(developer)

        then:
        "AuthenticationEndpoint was injected"
        authenticationEndpoint!=null

        and:
        "Response status is 401"
        response.getStatus() == Status.UNAUTHORIZED.getStatusCode()
    }

    def "test enroll without being logged in"() {
        given:
        "Non existing developer"
        def developer = buildDeveloper(UNAUTHORIZED_LOGIN_NAME, UNAUTHORIZED_PASSWORD)

        when:
        "Performing enroll"
        authenticationEndpoint.enroll(developer)

        and:
        "Retrieving the User from the underlying service"
        def newUser = (SimpleUser) identityManagement.findByUsername(UNAUTHORIZED_LOGIN_NAME)

        then:
        "Injections have been performed"
        authenticationEndpoint!=null && identityManagement != null

        and:
        "EJBException with an UnauthorizedException cause is thrown"
        def ex = thrown(EJBException)
        ex.cause instanceof UnauthorizedException

        and:
        "User does not exist"
        newUser == null
    }

    def "test logout without being logged in"() {
        when:
        "Performing logout"
        def response = authenticationEndpoint.logout()

        then:
        "AuthenticationEndpoint was injected"
        authenticationEndpoint!=null

        and:
        "Response status is 401"
        response.getStatus() == Status.UNAUTHORIZED.getStatusCode()
    }

    def "test logout being logged in"() {
        given:
        "Admin developer"
        def developer = buildDeveloper(AUTHORIZED_LOGIN_NAME, AUTHORIZED_PASSWORD)

        when:
        "Performing Login"
        def loginResponse = authenticationEndpoint.login(developer)

        and:
        "Performing logout"
        def logoutResponse = authenticationEndpoint.logout()

        and:
        "Trying to enroll while being logged out"
        authenticationEndpoint.enroll(developer)

        then:
        "AuthenticationEndpoint was injected"
        authenticationEndpoint!=null

        and:
        "Login response status is 200"
        loginResponse != null && loginResponse.getStatus() == Status.OK.getStatusCode()

        and:
        "Logout response status is 200"
        logoutResponse != null && logoutResponse.getStatus() == Status.OK.getStatusCode()

        and:
        "EJBException with an UnauthorizedException cause is thrown"
        def ex = thrown(EJBException)
        ex.cause instanceof UnauthorizedException
    }


    private Developer buildDeveloper(String loginName, String password) {
        Developer developer = new Developer()
        developer.setLoginName(loginName)
        developer.setPassword(password)
        return developer
    }
}