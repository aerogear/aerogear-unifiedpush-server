/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.aerogear.unifiedpush.rest;

import javax.inject.Inject;

import org.jboss.aerogear.unifiedpush.service.PushSearchService;

/**
 * Base class for all management RESTful endpoints. Offers hooks for common
 * features like SearchManager. SearchManager is a prototype bean, therefore
 * extend this class only for management related RESTfull.
 */
public abstract class AbstractManagementEndpoint extends AbstractBaseEndpoint {

	@Inject
	private SearchManager searchManager;

	/**
	 * offers PushSearchService to subclasses
	 *
	 * @return the push search service
	 */
	protected PushSearchService getSearch() {
		return searchManager.getSearchService();
	}
}
