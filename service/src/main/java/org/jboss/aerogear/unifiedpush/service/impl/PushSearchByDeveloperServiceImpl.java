package org.jboss.aerogear.unifiedpush.service.impl;

import org.jboss.aerogear.unifiedpush.api.PushApplication;
import org.jboss.aerogear.unifiedpush.api.Variant;
import org.jboss.aerogear.unifiedpush.dao.PageResult;
import org.jboss.aerogear.unifiedpush.dao.PushApplicationDao;
import org.jboss.aerogear.unifiedpush.dao.VariantDao;
import org.jboss.aerogear.unifiedpush.service.PushSearchService;
import org.jboss.aerogear.unifiedpush.service.annotations.LoggedIn;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;

public class PushSearchByDeveloperServiceImpl implements PushSearchService {

    @Inject
    private PushApplicationDao pushApplicationDao;

    @Inject
    private VariantDao variantDao;

    @Inject
    @LoggedIn
    private Instance<String> loginName;

    @Override
    public PageResult<PushApplication> findAllPushApplicationsForDeveloper(Integer page, Integer pageSize) {
        return pushApplicationDao.findAllForDeveloper(loginName.get(), page, pageSize);
    }

    @Override
    public PushApplication findByPushApplicationIDForDeveloper(String pushApplicationID) {

        return pushApplicationDao.findByPushApplicationIDForDeveloper(pushApplicationID, loginName.get());
    }

    @Override
    public Variant findByVariantIDForDeveloper(String variantID) {
        return variantDao.findByVariantIDForDeveloper(variantID, loginName.get());
    }

    @Override
    public boolean existsVariantIDForDeveloper(String variantID) {
        return variantDao.existsVariantIDForDeveloper(variantID, loginName.get());
    }
}
