package org.jboss.aerogear.unifiedpush.service.impl;

import org.jboss.aerogear.unifiedpush.api.PushApplication;
import org.jboss.aerogear.unifiedpush.dao.PageResult;
import org.jboss.aerogear.unifiedpush.dao.PushApplicationDao;
import org.jboss.aerogear.unifiedpush.service.SearchApplicationService;

import javax.inject.Inject;

public class SearchAllApplicationServiceImpl implements SearchApplicationService {

    @Inject
    private PushApplicationDao pushApplicationDao;

    @Override
    public PageResult<PushApplication> findAllPushApplicationsForDeveloper(String loginName, Integer page, Integer pageSize) {
        return pushApplicationDao.findAll();
    }

    @Override
    public PushApplication findByPushApplicationIDForDeveloper(String pushApplicationID, String loginName) {
        return pushApplicationDao.findAllByPushApplicationID(pushApplicationID);
    }


}
