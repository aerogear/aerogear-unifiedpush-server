/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.unifiedpush.rest;

import java.io.Serializable;

import javax.inject.Inject;

import org.jboss.aerogear.unifiedpush.service.PushSearchService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;

/**
 * Responsible for switch between different types of search Depending on the
 * role of the current logged in user
 */
@Controller
public class SearchManager implements Serializable {
	private static final long serialVersionUID = -6665967856424444078L;

	@Inject
	@Qualifier("PushSearchServiceImpl")
	private PushSearchService searchAll;
	@Inject
	@Qualifier("PushSearchByDeveloperServiceImpl")
	private PushSearchService searchByDeveloper;

	/**
	 * Validate the current logged in role
	 *
	 * @return an implementation of the search service
	 */
	public PushSearchService getSearchService() {

		boolean isAdmin = hasRole("ROLE_ADMIN");

		if (isAdmin) {
			return searchAll;
		}
		searchByDeveloper.setLoginName(AbstractBaseEndpoint.extractUsername());
		return searchByDeveloper;
	}

	protected boolean hasRole(String role) {
        // get security context from thread local
        SecurityContext context = SecurityContextHolder.getContext();

        if (context == null)
            return false;

        Authentication authentication = context.getAuthentication();
        if (authentication == null)
            return false;

        for (GrantedAuthority auth : authentication.getAuthorities()) {
            if (role.equals(auth.getAuthority()))
                return true;
        }

        return false;
    }
}
