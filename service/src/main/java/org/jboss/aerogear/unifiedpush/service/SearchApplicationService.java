package org.jboss.aerogear.unifiedpush.service;

import org.jboss.aerogear.unifiedpush.api.PushApplication;
import org.jboss.aerogear.unifiedpush.dao.PageResult;

/**
 * Base of the implementation for the admin/developer view
 */
public interface SearchApplicationService {

    /**
     * Finder that returns all pushApplication object for the given owner/developer.
     */
    PageResult<PushApplication> findAllPushApplicationsForDeveloper(String loginName, Integer page, Integer pageSize);

    /**
     * Finder that returns an actual PushApplication, identified by its ID and its owner/developer.
     */
    PushApplication findByPushApplicationIDForDeveloper(String pushApplicationID, String loginName);
}
