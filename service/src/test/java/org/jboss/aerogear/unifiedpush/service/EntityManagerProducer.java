package org.jboss.aerogear.unifiedpush.service;

import java.io.Serializable;

import javax.annotation.PreDestroy;
import javax.ejb.Stateful;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.Produces;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/**
 * Class to have OpenEJB produce/lookup a test EntityManager.
 */
@SessionScoped
@Stateful
public class EntityManagerProducer implements Serializable {
	private static final long serialVersionUID = -7838758820742154436L;

	{
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("UnifiedPush");
        entityManager = emf.createEntityManager();
    }

    private static EntityManager entityManager;

    @Produces
    public EntityManager produceEm() {

        if (! entityManager.getTransaction().isActive()) {
            entityManager.getTransaction().begin();
        }

        return entityManager;
    }

    @PreDestroy
    public void closeEntityManager() {
        if (entityManager.isOpen()) {
            entityManager.getTransaction().commit();
            entityManager.close();
        }
    }
}
