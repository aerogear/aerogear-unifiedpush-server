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
package org.jboss.aerogear.unifiedpush.service.impl;

import javax.inject.Inject;

import org.jboss.aerogear.unifiedpush.api.Variant;
import org.jboss.aerogear.unifiedpush.api.VariantType;
import org.jboss.aerogear.unifiedpush.dao.VariantDao;
import org.jboss.aerogear.unifiedpush.service.GenericVariantService;
import org.jboss.aerogear.unifiedpush.service.annotations.LoggedInUser;
import org.jboss.aerogear.unifiedpush.service.impl.spring.IKeycloakService;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class GenericVariantServiceImpl implements GenericVariantService {
	private static final Logger logger = org.slf4j.LoggerFactory.getLogger(GenericVariantServiceImpl.class);
	@Inject
	private IKeycloakService keycloakService;

	@Inject
	private VariantDao variantDao;

	@Autowired
	private CacheManager cacheManager;

	@Override
	public void addVariant(Variant variant, LoggedInUser user) {
		variant.setDeveloper(user.get());
		variantDao.create(variant);
	}

	@Override
	public Variant findByVariantID(String variantID) {
		return variantDao.findByVariantID(variantID);
	}

	/*
	 * Cacheable service
	 */
	private Variant find(String variantID) {
		Cache cache = cacheManager.getCache(GenericVariantService.CACHE_NAME);
		Variant var = (Variant) cache.get(variantID).get();
		if (var == null)
			cache.put(variantID, findByVariantID(variantID));
		return var;
	}

	@Override
	public Variant findVariantByKeycloakClientID(String clientId) {
		Variant variant = null;

		// Cacheable service
		Iterable<String> clientVariants = keycloakService.getVariantIdsFromClient(clientId);

		if (clientVariants != null) {
			for (String clientVariantId : clientVariants) {
				Variant clientVariant = find(clientVariantId);
				if (clientVariant != null && (clientVariant.getType() == VariantType.SIMPLE_PUSH)) {
					// TODO - Support case of several Variants.
					variant = clientVariant;
					break;
				}

			}
		}

		if (variant == null) {
			logger.info("unable to resolve variant for clientID={}", clientId);
		}

		return variant;
	}

	@Override
	public void updateVariant(Variant variant) {
		variantDao.update(variant);
		evict(variant.getId());
	}

	@Override
	public void removeVariant(Variant variant) {
		variantDao.delete(variant);
		evict(variant.getId());
	}

	private void evict(String clientId) {
		Cache cache = cacheManager.getCache(GenericVariantService.CACHE_NAME);
		cache.evict(clientId);
	}
}
