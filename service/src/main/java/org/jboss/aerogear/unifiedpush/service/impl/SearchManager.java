package org.jboss.aerogear.unifiedpush.service.impl;

import org.jboss.aerogear.unifiedpush.service.PushSearchService;
import org.jboss.aerogear.unifiedpush.service.annotations.LoggedIn;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.KeycloakSecurityContext;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;

/**
 * Responsible for switch between different types of search
 * Depending on the role of the current logged in user
 */
@RequestScoped
public class SearchManager implements Serializable {

    private HttpServletRequest httpServletRequest;

    @Inject
    private PushSearchServiceImpl searchAll;
    @Inject
    private PushSearchByDeveloperServiceImpl searchByDeveloper;

    public void setHttpServletRequest(HttpServletRequest httpServletRequest){
        this.httpServletRequest = httpServletRequest;
    }

    /**
     * Validate the current logged in role
     * @return an implementation of the search service
     */
    public PushSearchService getSearch() {

        boolean isAdmin = httpServletRequest.isUserInRole("admin");

        if (isAdmin) {
            return searchAll;
        }
        return searchByDeveloper;
    }

    /**
     * Extract the username to be used in multiple queries
     * @return current logged in user
     */
    @Produces
    @LoggedIn
    public String extractUsername() {
        KeycloakPrincipal p = (KeycloakPrincipal) httpServletRequest.getUserPrincipal();
        KeycloakSecurityContext kcSecurityContext = p.getKeycloakSecurityContext();
        return kcSecurityContext.getToken().getPreferredUsername();

    }
}
