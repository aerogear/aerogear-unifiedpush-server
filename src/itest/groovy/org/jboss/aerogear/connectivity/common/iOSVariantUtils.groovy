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
package org.jboss.aerogear.connectivity.common

import groovy.json.JsonBuilder

import org.jboss.aerogear.connectivity.rest.util.iOSApplicationUploadForm

import com.jayway.restassured.RestAssured

class iOSVariantUtils {

    def createiOSApplicationUploadForm(Boolean production, String passphrase, byte[] certificate,
            String name, String description) {

        def ios = new iOSApplicationUploadForm()

        ios.setCertificate(certificate)
        ios.setDescription(description)
        ios.setName(name)
        ios.setPassphrase(passphrase)
        ios.setProduction(production)

        return ios
    }

    def registerIOsVariant(String pushAppId, iOSApplicationUploadForm form, Map<String, ?> cookies,
            String certificatePath) {

        assert root !=null

        JsonBuilder json = new JsonBuilder()
        def response = RestAssured.given()
                .contentType("multipart/form-data")
                .header("Accept", "application/json")
                .cookies(cookies)
                .multiPart("certificate", new File(certificatePath))
                .multiPart("production", form.getProduction().toString())
                .multiPart("passphrase", form.getPassphrase())
                .multiPart("name", form.getName())
                .multiPart("description", form.getDescription())
                .post("${root}rest/applications/${pushAppId}/iOS")

        return response
    }
}
