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
package org.jboss.aerogear.connectivity.jpa.dao.impl;

import javax.enterprise.inject.Default
import javax.enterprise.inject.Produces
import javax.inject.Inject
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext
import javax.persistence.PersistenceContextType

import org.jboss.aerogear.connectivity.api.Variant
import org.jboss.aerogear.connectivity.jpa.PersistentObject
import org.jboss.aerogear.connectivity.jpa.dao.PushApplicationDao
import org.jboss.aerogear.connectivity.model.PushApplication
import org.jboss.arquillian.container.test.api.Deployment
import org.jboss.arquillian.persistence.UsingDataSet
import org.jboss.arquillian.spock.ArquillianSpecification
import org.jboss.arquillian.transaction.api.annotation.TransactionMode
import org.jboss.arquillian.transaction.api.annotation.Transactional
import org.jboss.shrinkwrap.api.Archive
import org.jboss.shrinkwrap.api.ShrinkWrap
import org.jboss.shrinkwrap.api.spec.JavaArchive
import org.jboss.shrinkwrap.resolver.api.maven.Maven

import spock.lang.Specification

@ArquillianSpecification
class PushDaoSpecification extends Specification {

    @Deployment
    def static Archive testArchive() {
        JavaArchive jar = ShrinkWrap.create(JavaArchive.class)
                .addClasses(PushApplicationDao.class, PushApplicationDaoImpl.class) // tested classes
                .addPackage(PushApplication.class.getPackage()) // model
                .addPackage(PersistentObject.class.getPackage()) // jpa
                .addPackage(Variant.class.getPackage()) // api
                .addAsManifestResource("META-INF/beans.xml", "beans.xml")
                .addAsResource("META-INF/persistence.xml")

        // temporary hack to solve https://issues.jboss.org/browse/ARQ-1429
        JavaArchive asm = Maven.resolver().resolve("org.ow2.asm:asm:4.1").withoutTransitivity().asSingle(JavaArchive.class)
        jar = jar.merge(asm)

        // System.out.println(jar.toString(true));

        return jar
    }

    @Produces
    @PersistenceContext(unitName = "pushee-default", type = PersistenceContextType.EXTENDED)
    @Default
    EntityManager entityManager

    @Inject
    PushApplicationDao pushAppDao;

    def "find all registered apps"() {
        when: "Check for all registered apps"
        List<PushApplication> apps = pushAppDao.findAll();

        then: "DAO was injected"
        pushAppDao!=null
        and: "No applications were defined"
        apps.size()==0
    }


    @UsingDataSet("pushapps.yml")
    // FIXME if running in default (COMMIT) mode, APE fails to find the transaction to commit
    @Transactional(value = TransactionMode.DISABLED)
    def "find an app registered by APE"() {
        when: "Check for all registered apps"
        List<PushApplication> apps = pushAppDao.findAll();

        then: "DAO was injected"
        pushAppDao!=null
        and: "One application was defined"
        apps.size() == 1
    }

}
