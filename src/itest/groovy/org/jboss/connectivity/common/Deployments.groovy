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
package org.jboss.connectivity.common

import org.jboss.shrinkwrap.api.ShrinkWrap
import org.jboss.shrinkwrap.api.spec.WebArchive
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.archive.importer.MavenImporter

class Deployments {

    def static WebArchive unifiedPushServer() {

        def unifiedPushServerPom = System.getProperty("unified.push.server.location", "pom.xml")

        WebArchive war = ShrinkWrap.create(MavenImporter.class).loadPomFromFile(unifiedPushServerPom).importBuildOutput()
                .as(WebArchive.class);

        // replace original persistence.xml with testing one
        war.delete("/WEB-INF/classes/META-INF/persistence.xml")
        // testing persistence
        war.addAsResource("META-INF/persistence.xml")
        return war
    }

    def static WebArchive unifiedPushServerWithClasses(Class... clazz) {
        def unifiedPushServerPom = System.getProperty("unified.push.server.location", "pom.xml")

        WebArchive war = ShrinkWrap.create(MavenImporter.class).loadPomFromFile(unifiedPushServerPom).importBuildOutput()
                .as(WebArchive.class)

        war.delete("/WEB-INF/classes/META-INF/persistence.xml")
        war.addAsResource("META-INF/test-persistence.xml", "META-INF/persistence.xml")

        war.delete("/WEB-INF/classes/META-INF/beans.xml")
        war.addAsResource("META-INF/test-beans.xml", "META-INF/beans.xml")

        war.delete("/WEB-INF/h2-ds.xml")
        war.addAsWebInfResource("WEB-INF/test-h2-ds.xml", "h2-ds.xml")

        war.addClasses(clazz)

        File[] asm = Maven.resolver().resolve("org.ow2.asm:asm:4.1").withoutTransitivity().asFile()
        war = war.addAsLibraries(asm)

        File[] libs = Maven.resolver().loadPomFromFile("pom.xml").resolve(
                "org.mockito:mockito-core").withTransitivity().asFile()
        war = war.addAsLibraries(libs)

        return war
    }
}

