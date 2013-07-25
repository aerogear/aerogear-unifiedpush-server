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
package org.jboss.aerogear.connectivity.simplepush

import java.net.URL;


import com.jayway.restassured.RestAssured
import groovy.json.JsonBuilder

import org.jboss.arquillian.container.test.api.Deployment
import org.jboss.arquillian.test.api.ArquillianResource
import org.jboss.shrinkwrap.api.spec.WebArchive
import spock.lang.Specification

import spock.lang.Shared

import org.jboss.arquillian.spock.ArquillianSpecification
import org.jboss.connectivity.common.AdminLogin;
import org.jboss.connectivity.common.Deployments;

@ArquillianSpecification
@Mixin(AdminLogin)
class RegisterVariantsSpecification extends Specification {

    @ArquillianResource
    URL root

    @Deployment(testable=false)
    def static WebArchive "create deployment"() {
        Deployments.unifiedPushServer()
    }

    // push application ID is reused in registering mobile variant
    @Shared def pushAppId
    @Shared def authCookies

    def setup() {
        authCookies = authCookies ? authCookies : login()
    }

    // curl -v -H "Accept: application/json" -H "Content-type: application/json" -X POST -d '{"name" : "ddd", "description" :  "ddd" }' http://localhost:8080/ag-push/rest/
    def "Registering a push application"() {

        given: "Application My App is about to be registered......"
        def json = new JsonBuilder()
        def request = RestAssured.given()
                .contentType("application/json")
                .header("Accept", "application/json")
                .cookies(authCookies)
                .body(json {
                    name "ddd"
                    description "ddd"
                })

        when: "Application is registered"
        def response = RestAssured.given().spec(request).post("${root}rest/applications")
        def body = response.body().jsonPath()
        pushAppId = body.get("pushApplicationID")

        then: "Response code 201 is returned"
        response.statusCode() == 201

        and: "Push App Id is not null"
        pushAppId != null

        and: "AppName is not null"
        body.get("name") == "ddd"
    }


    // curl -v -H "Accept: application/json" -H "Content-type: application/json" -X POST -d '{"pushNetworkURL" : "http://localhost:7777/endpoint/"}' http://localhost:8080/ag-push/rest/applications/{PUSH_ID}/simplePush
    def "Registering a simple push mobile variant instance"() {

        given: "Variant of ddd application is about to be registered......"
        def json = new JsonBuilder()
        def request = RestAssured.given()
                .contentType("application/json")
                .header("Accept", "application/json")
                .cookies(authCookies)
                .body(json {
                    name "ddd simple push variant"
                    pushNetworkURL "http://localhost:7777/endpoint/"
                })

        when: "Variant is registered"
        def response = RestAssured.given().spec(request).post("${root}rest/applications/${pushAppId}/simplePush ")
        def body = response.body().jsonPath()

        then: "Response code 201 is returned"
        response.statusCode() == 201

        and: "Variant Id is not null"
        body.get("variantID") != null

        and: "Variant name is not null"
        body.get("name") == "ddd simple push variant"

    }
}

