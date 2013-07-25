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
package org.jboss.aerogear.connectivity.restassured

import groovy.json.JsonBuilder
import groovy.json.JsonOutput
import spock.lang.Specification

class JsonParserSpecification extends Specification {

    def "JSON map and non-map syntax"() {
        given: "Two JSON builders"
        def json = new JsonBuilder();
        def json2 = new JsonBuilder();

        when: "Creating a JSON using map and non-map syntax"
        def string1 = JsonOutput.toJson(json.call() {
            foo: "bar"
            name: "namebar"
        });

        def string2 = JsonOutput.toJson(json {
            foo "bar"
            name "namebar"
        });

        then: "Map syntax yields empty JSON object"
        string1 == "{}"

        and: "Non-map syntax yields correct JSON object"
        string2 == '{"foo":"bar","name":"namebar"}'
    }
}
