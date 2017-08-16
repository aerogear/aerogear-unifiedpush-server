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
import javax.servlet.http.HttpServletRequest;

import org.jboss.aerogear.unifiedpush.service.PushSearchService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Controller;

/**
 * Responsible for switch between different types of search Depending on the
 * role of the current logged in user
 */
@Controller
@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class SearchManager implements Serializable {
	private static final long serialVersionUID = -6665967856424444078L;

	@Inject
	private HttpServletRequest httpServletRequest;

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

		boolean isAdmin = httpServletRequest.isUserInRole("admin");

		if (isAdmin) {
			return searchAll;
		}
		searchByDeveloper.setLoginName(AbstractBaseEndpoint.extractUsername(httpServletRequest));
		return searchByDeveloper;
	}
}
