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

import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;

import org.jboss.aerogear.unifiedpush.api.PushApplication;
import org.jboss.aerogear.unifiedpush.api.Variant;
import org.jboss.aerogear.unifiedpush.dao.PushApplicationDao;
import org.jboss.aerogear.unifiedpush.service.AliasService;
import org.jboss.aerogear.unifiedpush.service.DocumentService;
import org.jboss.aerogear.unifiedpush.service.PushApplicationService;
import org.jboss.aerogear.unifiedpush.service.annotations.LoggedInUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PushApplicationServiceImpl implements PushApplicationService {
	@Inject
	private PushApplicationDao pushApplicationDao;

	@Inject
	private DocumentService documentService;
	@Inject
	private AliasService aliasService;
	@Autowired
	private CacheManager cacheManager;

	public PushApplicationServiceImpl() {
	}

	@Override
	public void addPushApplication(PushApplication pushApp, LoggedInUser user) {
		pushApp.setDeveloper(user.get());
		pushApplicationDao.create(pushApp);
	}

	@Override
	public PushApplication findByPushApplicationID(String pushApplicationID) {
		return pushApplicationDao.findByPushApplicationID(pushApplicationID);
	}

	@Override
	public void addVariant(PushApplication pushApp, Variant variant) {
		pushApp.getVariants().add(variant);
		pushApplicationDao.update(pushApp);
	}

	@Override
	public Map<String, Long> countInstallationsByType(String pushApplicationID) {
		return pushApplicationDao.countInstallationsByType(pushApplicationID);
	}

	@Override
	public void updatePushApplication(PushApplication pushApp) {
		pushApplicationDao.update(pushApp);
	}

	@Override
	public void removePushApplication(PushApplication pushApp) {
		// Evict All caches
		pushApp.getVariants().stream().forEach(var -> evictByVariantId(var.getVariantID()));
		evictById(pushApp.getPushApplicationID());
		evictByName(pushApp.getName());

		// Delete push application
		pushApplicationDao.delete(pushApp);

		// delete aliases
		aliasService.removeAll(UUID.fromString(pushApp.getPushApplicationID()));

		// Delete any application documents
		documentService.delete(pushApp.getPushApplicationID());
	}

	@Override
	public PushApplication findByVariantID(String variantId) {
		return pushApplicationDao.findByVariantId(variantId);
	}

	@Override
	public PushApplication findByName(String name) {
		return pushApplicationDao.findByPushApplicationName(name);
	}

	private void evictByVariantId(String variantId) {
		Cache cache = cacheManager.getCache(PushApplicationService.APPLICATION_CACHE_BY_VAR_ID);
		cache.evict(variantId);
	}

	private void evictById(String id) {
		Cache cache = cacheManager.getCache(PushApplicationService.APPLICATION_CACHE_BY_ID);
		cache.evict(id);
	}

	private void evictByName(String name) {
		Cache cache = cacheManager.getCache(PushApplicationService.APPLICATION_CACHE_BY_NAME);
		cache.evict(name);
	}
}
