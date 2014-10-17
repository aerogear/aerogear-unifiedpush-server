package org.jboss.aerogear.unifiedpush.service.impl;

import org.jboss.aerogear.unifiedpush.api.PushApplication;
import org.jboss.aerogear.unifiedpush.dao.PageResult;
import org.jboss.aerogear.unifiedpush.dao.PushApplicationDao;
import org.jboss.aerogear.unifiedpush.service.SearchApplicationService;
import org.jboss.aerogear.unifiedpush.service.annotations.LoggedIn;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;

public class SearchByDeveloperApplicationServiceImpl implements SearchApplicationService {

    @Inject
    private PushApplicationDao pushApplicationDao;

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

}
