package org.jboss.aerogear.unifiedpush.jpa.dao.impl;

import org.jboss.aerogear.unifiedpush.jpa.AbstractGenericDao;
import org.jboss.aerogear.unifiedpush.jpa.dao.ChromePackagedAppVariantDao;
import org.jboss.aerogear.unifiedpush.model.ChromePackagedAppVariant;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: lholmquist
 * Date: 8/1/13
 * Time: 10:39 PM
 * To change this template use File | Settings | File Templates.
 */
public class ChromePackagedAppDaoImpl extends AbstractGenericDao<ChromePackagedAppVariant, String> implements ChromePackagedAppVariantDao {
    @SuppressWarnings("unchecked")
    @Override
    public List<ChromePackagedAppVariant> findAll() {
        return createQuery("select t from " + ChromePackagedAppVariant.class.getSimpleName() + " t").getResultList();
    }

    @Override
    public ChromePackagedAppVariant findByVariantIDForDeveloper(String variantID, String loginName) {
        return getSingleResultForQuery(createQuery(
                "select t from " + ChromePackagedAppVariant.class.getSimpleName() + " t where t.variantID = :variantID and t.developer = :developer")
                .setParameter("variantID", variantID)
                .setParameter("developer", loginName));
    }
}
