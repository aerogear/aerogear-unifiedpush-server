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
package org.jboss.connectivity.common

import groovy.json.JsonBuilder

import com.jayway.restassured.RestAssured

// FIXME this should be done via inheritance, see https://issues.jboss.org/browse/ARQ-1427
class AdminLogin {

    static final String NEWPASSWORD = "aerogear"

    def login() {
        assert root !=null

        // login with default password
        def json = new JsonBuilder()
        def response = RestAssured.given()
                .contentType("application/json")
                .header("Accept", "application/json")
                .body( json {
                    loginName "admin"
                    password "123"
                })
                .post("${root}rest/auth/login")

        // we need to change the password
        if(response.getStatusCode()==205) {
            def cookies = response.getDetailedCookies()
            assert cookies !=null
            response = RestAssured.given()
                    .contentType("application/json")
                    .header("Accept", "application/json")
                    .cookies(cookies)
                    .body( json {
                        loginName "admin"
                        password NEWPASSWORD
                    })
                    .put("${root}rest/auth/update")

            assert response.getStatusCode() == 200
        }

        // try to login with new password
        response = RestAssured.given()
                .contentType("application/json")
                .header("Accept", "application/json")
                .body( json {
                    loginName "admin"
                    password NEWPASSWORD
                })
                .expect()
                .statusCode(200)
                .when().post("${root}rest/auth/login")

        // return cookies
        response.getDetailedCookies()
    }
}
