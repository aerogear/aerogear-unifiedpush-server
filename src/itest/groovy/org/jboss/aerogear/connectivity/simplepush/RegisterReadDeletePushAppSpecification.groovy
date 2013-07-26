/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.connectivity.simplepush;


import com.jayway.restassured.RestAssured
import com.jayway.restassured.filter.log.RequestLoggingFilter
import com.jayway.restassured.filter.log.ResponseLoggingFilter
import groovy.json.JsonBuilder
import org.jboss.arquillian.container.test.api.Deployment
import org.jboss.arquillian.spock.ArquillianSpecification
import org.jboss.arquillian.test.api.ArquillianResource
import org.jboss.aerogear.connectivity.common.AdminLogin
import org.jboss.aerogear.connectivity.common.Deployments
import org.jboss.shrinkwrap.api.spec.WebArchive
import spock.lang.Shared
import spock.lang.Specification
import javax.inject.Inject
import org.jboss.aerogear.connectivity.jpa.dao.PushApplicationDao
import javax.enterprise.inject.Produces
import javax.persistence.PersistenceContext
import javax.persistence.PersistenceContextType
import javax.enterprise.inject.Default
import javax.persistence.EntityManager
import groovy.json.JsonSlurper

@ArquillianSpecification
@Mixin(AdminLogin)
class RegisterReadDeletePushAppSpecification extends Specification {


    @ArquillianResource
    URL root

    @Deployment(testable=false)
    def static WebArchive "create deployment"() {
        Deployments.unifiedPushServer()
    }

    @Shared def authCookies
    @Shared def pushAppId

    def setup() {
        authCookies = authCookies ? authCookies : login()
        RestAssured.filters(new RequestLoggingFilter(System.err), new ResponseLoggingFilter(System.err))
    }

    def "Registering a push application"() {

        given: "Application RegisterReadDeletePushAppSpecification is about to be registered"
        def json = new JsonBuilder()
        def request = RestAssured.given()
                .contentType("application/json")
                .header("Accept", "application/json")
                .cookies(authCookies)
                .body( json {
            name "RegisterReadDeletePushAppSpecification"
            description "RegisterReadDeletePushAppSpecification desc"
        })

        when: "Application is registered"
        def response = RestAssured.given().spec(request).post("${root}rest/applications")
        def body = response.body().jsonPath()
        pushAppId = body.get("pushApplicationID")

        then: "Response code 201 is returned"
        response.statusCode() == 201

        and: "Push App Id is not null"
        pushAppId != null

        and: "Push App Name is RegisterReadDeletePushAppSpecification"
        body.get("name") == "RegisterReadDeletePushAppSpecification"

        and: "Push App Description is RegisterReadDeletePushAppSpecification desc"
        body.get("description") == "RegisterReadDeletePushAppSpecification desc"
    }

    def "Retrieve all push applications and found newly registered one in the list"() {

        given: "Read all registered apps"
        def request = RestAssured.given()
                .contentType("application/json")
                .header("Accept", "application/json")
                .cookies(authCookies)

        when: "Apps are retrieved"
        def response = RestAssured.given().spec(request).get("${root}rest/applications")
        def responseString = response.asString()
        def slurper = new JsonSlurper()
        def apps = slurper.parseText responseString

        then: "Response code 200 is returned"
        response.statusCode() == 200

        and: "pushAppId is in the list"
        def found = apps.find {
            it.get("pushApplicationID") == pushAppId
        }
        found != null
        found.name == "RegisterReadDeletePushAppSpecification"
    }

    def "Retrieve registered application"() {

        given: "Read my app RegisterReadDeletePushAppSpecification"
        def request = RestAssured.given()
                .contentType("application/json")
                .header("Accept", "application/json")
                .cookies(authCookies)


        when: "Application is retrieved"
        def response = RestAssured.given().spec(request).get("${root}rest/applications/${pushAppId}")
        def responseString = response.asString()
        def slurper = new JsonSlurper()
        def responseObject = slurper.parseText responseString

        then: "Response code 200 is returned"
        response.statusCode() == 200

        and: "App name is RegisterReadDeletePushAppSpecification"
        responseObject.get("pushApplicationID") == pushAppId
        responseObject.name == "RegisterReadDeletePushAppSpecification"
    }

    def "Delete registered push app"() {

        given: "Delete RegisterReadDeletePushAppSpecification"
        def json = new JsonBuilder()
        def request = RestAssured.given()
                .contentType("application/json")
                .header("Accept", "application/json")
                .cookies(authCookies)
                .body( json {
                    'pushAppId' pushAppId
                     })

        when: "Application is deleted"
        def response = RestAssured.given().spec(request).delete("${root}rest/applications/${pushAppId}")
        def responseString = response.asString()

        then: "Response code 204 is returned"
        response.statusCode() == 204

        and: "Content is empty"
        responseString == ""
    }
}
