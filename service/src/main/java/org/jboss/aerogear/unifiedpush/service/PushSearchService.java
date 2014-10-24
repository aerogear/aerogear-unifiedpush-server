package org.jboss.aerogear.unifiedpush.service;

import org.jboss.aerogear.unifiedpush.api.PushApplication;
import org.jboss.aerogear.unifiedpush.dao.PageResult;
import org.jboss.aerogear.unifiedpush.service.dashboard.Application;
import org.jboss.aerogear.unifiedpush.service.dashboard.ApplicationVariant;
import org.jboss.aerogear.unifiedpush.service.dashboard.DashboardData;

import java.util.List;

/**
 * Base of the implementation for the admin/developer view
 */
public interface PushSearchService {

    /**
     * Finder that returns all pushApplication object for the given owner/developer.
     */
    PageResult<PushApplication> findAllPushApplicationsForDeveloper(Integer page, Integer pageSize);

    /**
     * Finder that returns an actual PushApplication, identified by its ID and its owner/developer.
     */
    PushApplication findByPushApplicationIDForDeveloper(String pushApplicationID);

    boolean existsVariantIDForDeveloper(String variantID);

    DashboardData loadDashboardData();

    /**
     * Loads all the Variant objects where we did notice some failures on sending
     * for the given user
     */
    List<ApplicationVariant> getVariantsWithWarnings();

    /**
     * Loads all the Variant objects with the most received messages
     */
    List<Application> getTopThreeLastActivity();


}
