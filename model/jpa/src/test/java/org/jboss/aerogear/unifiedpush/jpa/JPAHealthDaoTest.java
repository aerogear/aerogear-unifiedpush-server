package org.jboss.aerogear.unifiedpush.jpa;

import org.jboss.aerogear.unifiedpush.jpa.dao.impl.JPAHealthDao;
import org.jboss.aerogear.unifiedpush.utils.DaoDeployment;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import javax.persistence.EntityManager;


@RunWith(Arquillian.class)
public class JPAHealthDaoTest  {


    @Inject
    private EntityManager entityManager;
    @Inject
    private JPAHealthDao healthDao;

    @Deployment
    public static JavaArchive createDeployment() {
        return DaoDeployment.createDeployment();
    }

    @Test
    public void testDbCheck() throws Exception {
        healthDao.dbCheck();
    }
}