package org.jboss.aerogear.unifiedpush.service.impl;

import org.jboss.aerogear.unifiedpush.service.SearchApplicationService;
import org.jboss.aerogear.unifiedpush.service.annotations.SearchService;

import javax.enterprise.inject.New;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;


public class SearchManager {

    private final HttpServletRequest httpServletRequest;

    @Inject
    public SearchManager(HttpServletRequest httpServletRequest) {
        this.httpServletRequest = httpServletRequest;
    }

    @Produces
    @SearchService
    public SearchApplicationService getSearch(@New SearchAllApplicationServiceImpl searchAll,
                                              @New SearchByDeveloperApplicationServiceImpl searchByDeveloper) {

        boolean isAdmin = httpServletRequest.isUserInRole("admin");

        if (isAdmin) {
            return searchAll;
        }
        return searchByDeveloper;
    }

}
