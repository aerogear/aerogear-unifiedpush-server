package org.jboss.aerogear.unifiedpush.jpa.dao.impl;

import javax.inject.Inject;
import javax.persistence.EntityManager;

public class JPAHealthDao {
    @Inject
    private EntityManager entityManager;

    public boolean dbCheck() {
        return entityManager.createNativeQuery("select 1 from PushApplication").getFirstResult() == 1;
    }
}
