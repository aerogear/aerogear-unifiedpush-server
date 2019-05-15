package org.jboss.aerogear.unifiedpush.service;

import javax.annotation.PreDestroy;
import javax.ejb.Stateful;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.Produces;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.io.Serializable;

/**
 * Static class to have OpenEJB produce/lookup a test EntityManager.
 */
@SessionScoped
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

    @PreDestroy
    public void closeEntityManager() {
        if (entityManager.isOpen()) {
            entityManager.close();
        }
    }
}
