package org.jboss.aerogear.unifiedpush.utils;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.persistence.EntityManager;
import javax.persistence.Persistence;

/**
 * CDI Utility class, which produces a entity manager
 */
public class EntityFactory {

    @Produces
    @ApplicationScoped
    private EntityManager entityManage() {
        return Persistence.createEntityManagerFactory("UnifiedPush").createEntityManager();
    }
}
