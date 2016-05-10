package org.jboss.aerogear.unifiedpush.jpa;

import org.jboss.aerogear.unifiedpush.utils.DaoDeployment;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.net.UnknownHostException;


@RunWith(Arquillian.class)
public class MyMongoClientTest  {

    @Deployment
    public static JavaArchive createDeployment() {
        return DaoDeployment.createDeployment();
    }

    @Inject
    EntityManager entityManager;

    @Test
    public void testMyMongoClient()
    {

        try {
            System.out.print(MyMongoClient.getDB(entityManager).getMongo());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }


    }

}