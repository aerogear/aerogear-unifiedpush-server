/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.unifiedpush.jpa;

import org.jboss.aerogear.unifiedpush.jpa.dao.impl.Person;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

@RunWith(Arquillian.class)
public class DatabasePingTest {


    private static EntityManagerFactory entityManagerFactory;


    @Deployment
    public static JavaArchive createDeployment() {
        JavaArchive war =  ShrinkWrap.create(JavaArchive.class)

                .addClass(Person.class)
                        //.addPackage("org.jboss.aerogear.unifiedpush.jpa.dao.impl")
                        //.addPackage("org.jboss.aerogear.unifiedpush.api")
                        //.addPackage("org.jboss.aerogear.unifiedpush.api.dao")
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsManifestResource("META-INF/persistence.xml");

        System.out.println(war.toString(true));
        return war;
    }

    @BeforeClass
    public static void setUpEntityManagerFactory() {
        entityManagerFactory = Persistence.createEntityManagerFactory( "UnifiedPush" );
    }

    @AfterClass
    public static void closeEntityManagerFactory() {
        entityManagerFactory.close();
    }


    @Test
    public void findAllIDsForDeveloper() {
        Assert.assertNull("foo", null);
    }


}
