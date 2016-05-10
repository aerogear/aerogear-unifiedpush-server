package org.jboss.aerogear.unifiedpush.jpa;


import com.mongodb.DB;
import com.mongodb.MongoClient;

import javax.persistence.EntityManager;
import java.net.UnknownHostException;

public class MyMongoClient {


    public static DB getDB(EntityManager entityManager) throws UnknownHostException{
        Object hib_host = entityManager.getEntityManagerFactory()
                .getProperties().get("hibernate.ogm.datastore.host");
        Object hib_port = entityManager.getEntityManagerFactory()
                .getProperties().get("hibernate.ogm.datastore.port");


        String host;
        int port;
        String db_name = (String) entityManager.getEntityManagerFactory()
                .getProperties().get("hibernate.ogm.datastore.database");


        // if not provided set according hibernate ogm default values
        if (hib_host == null)
            host = "127.0.0.1";
        else
            host = (String) hib_host;


        if (hib_port == null)
            port = 27017;
        else
            port = Integer.valueOf((String) hib_port);

        return new MongoClient(host,port).getDB(db_name);
    }


}
