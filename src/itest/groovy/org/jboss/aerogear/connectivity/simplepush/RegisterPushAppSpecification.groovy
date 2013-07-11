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
package org.jboss.aerogear.connectivity.simplepush;

import java.net.URL;

import groovy.json.JsonBuilder
import groovy.json.JsonOutput;

import org.jboss.arquillian.container.test.api.Deployment
import org.jboss.arquillian.spock.ArquillianSpecification;
import org.jboss.arquillian.test.api.ArquillianResource
import org.jboss.connectivity.common.AdminLogin;
import org.jboss.connectivity.common.Deployments;
import org.jboss.modules.AssertionSetting;
import org.jboss.shrinkwrap.api.ShrinkWrap
import org.jboss.shrinkwrap.api.spec.WebArchive

import spock.lang.Shared
import spock.lang.Specification

import com.jayway.restassured.RestAssured

/**
 *
 * @author <a href="kpiwko@redhat.com">Karel Piwko</a>
 *
 */
@ArquillianSpecification
@Mixin(AdminLogin)
class RegisterPushAppSpecification extends Specification {

    @ArquillianResource
    URL root

    @Deployment(testable=false)
    def static WebArchive "create deployment"() {
        Deployments.unifiedPushServer()
    }

    @Shared def authCookies

    def setup() {
        authCookies = authCookies ? authCookies : login()
    }

    // curl -v -H "Accept: application/json" -H "Content-type: application/json" -X POST -d '{"name" : "MyApp", "description" :  "awesome app" }'
    // http://localhost:8080/ag-push/rest/applications
    def "Registering a push application"() {

        given: "Application My App is about to be registered"
        def json = new JsonBuilder()
        def request = RestAssured.given()
                .contentType("application/json")
                .header("Accept", "application/json")
                .cookies(authCookies)
                .body( json {
                    name "MyApp"
                    description "awesome app"
                })

        when: "Application is registered"
        def response = RestAssured.given().spec(request).post("${root}rest/applications")
        def body = response.body().jsonPath()

        then: "Response code 201 is returned"
        response.statusCode() == 201

        and: "Push App Id is not null"
        body.get("pushApplicationID") != null

        and: "Push App Name is MyApp"
        body.get("name") == "MyApp"
    }

    def "Registering a push application with charset"() {

        given: "Application My App is about to be registered"
        def json = new JsonBuilder()
        def request = RestAssured.given()
                .contentType("application/json; charset=utf-8")
                .header("Accept", "application/json")
                .cookies(authCookies)
                .body( json {
                    name "MyUtf8App"
                    description "Awesome UTF8 App"
                })

        when: "Application is registered"
        def response = RestAssured.given().spec(request).post("${root}rest/applications")
        def body = response.body().jsonPath()

        then: "Response code 201 is returned"
        response.statusCode() == 201

        and: "Push App Id is not null"
        body.get("pushApplicationID") != null

        and: "Push App Name is MyApp"
        body.get("name") == "MyUtf8App"
    }
}
