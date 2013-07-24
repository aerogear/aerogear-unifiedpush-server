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


import com.jayway.restassured.RestAssured
import groovy.json.JsonBuilder
import org.jboss.arquillian.container.test.api.Deployment
import org.jboss.arquillian.spock.ArquillianSpecification
import org.jboss.arquillian.test.api.ArquillianResource
import org.jboss.aerogear.connectivity.common.AdminLogin
import org.jboss.aerogear.connectivity.common.Deployments
import org.jboss.shrinkwrap.api.spec.WebArchive
import spock.lang.Shared
import spock.lang.Specification
import com.jayway.restassured.filter.log.RequestLoggingFilter
import com.jayway.restassured.filter.log.ResponseLoggingFilter
import org.apache.http.entity.ContentType

@ArquillianSpecification
@Mixin(AdminLogin)
class RegisterPushAppWithoutLoginSpecification extends Specification {

    @ArquillianResource
    URL root

    @Deployment(testable=false)
    def static WebArchive "create deployment"() {
        Deployments.unifiedPushServer()
    }

    def setup() {
        //RestAssured.filters(new RequestLoggingFilter(System.err), new ResponseLoggingFilter(System.err))
    }

    def "Registering a push application without being logged"() {

        given: "Trying to register application My App"
        def json = new JsonBuilder()
        def request = RestAssured.given()
                .contentType("application/json")
                .header("Accept", "application/json")
                .body( json {
                    name "MyApp"
                    description "Not logged in app"
                })

        when: "Application is registered"
        def response = RestAssured.given().spec(request).post("${root}rest/applications")
        def responseString = response.asString()

        then: "Response code 401 is returned"
        response.statusCode() == 401

        and: "Error message"
        responseString == "{message : User authentication failed }"
    }

}
