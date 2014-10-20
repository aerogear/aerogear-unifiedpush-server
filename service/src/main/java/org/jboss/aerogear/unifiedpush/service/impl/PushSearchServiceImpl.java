package org.jboss.aerogear.unifiedpush.service.impl;

import org.jboss.aerogear.unifiedpush.api.PushApplication;
import org.jboss.aerogear.unifiedpush.api.Variant;
import org.jboss.aerogear.unifiedpush.dao.PageResult;
import org.jboss.aerogear.unifiedpush.dao.PushApplicationDao;
import org.jboss.aerogear.unifiedpush.dao.VariantDao;
import org.jboss.aerogear.unifiedpush.service.PushSearchService;

import javax.inject.Inject;

public class PushSearchServiceImpl implements PushSearchService {

    @Inject
    private PushApplicationDao pushApplicationDao;

    @Inject
    private VariantDao variantDao;

    @Override
    public PageResult<PushApplication> findAllPushApplicationsForDeveloper(Integer page, Integer pageSize) {
        return pushApplicationDao.findAll(page, pageSize);
    }

    @Override
    public PushApplication findByPushApplicationIDForDeveloper(String pushApplicationID) {
        return pushApplicationDao.findAllByPushApplicationID(pushApplicationID);
    }

    @Override
    public Variant findByVariantIDForDeveloper(String variantID) {
        return variantDao.findAllByVariantID(variantID);
    }

    @Override
    public boolean existsVariantIDForDeveloper(String variantID) {
        return variantDao.existsVariantIDForAdmin(variantID);
    }


}
