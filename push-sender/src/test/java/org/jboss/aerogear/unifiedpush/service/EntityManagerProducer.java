package org.jboss.aerogear.unifiedpush.service;

import javax.ejb.Stateful;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.io.Serializable;

/**
 * Static class to have OpenEJB produce/lookup a test EntityManager.
 */
@ApplicationScoped
@Stateful
public class EntityManagerProducer implements Serializable {

    {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("UnifiedPush");
        entityManager = emf.createEntityManager();
    }

    private static EntityManager entityManager;

    @Produces
    public EntityManager produceEm() {
        return entityManager;
    }

    public static void rollback() {
        entityManager.getTransaction().rollback();

        entityManager.getTransaction().begin();
    }
}
