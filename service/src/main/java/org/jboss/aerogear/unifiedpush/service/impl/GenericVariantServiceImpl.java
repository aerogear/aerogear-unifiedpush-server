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
import java.util.concurrent.ConcurrentHashMap;

import javax.ejb.Stateless;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.jboss.aerogear.unifiedpush.api.Variant;
import org.jboss.aerogear.unifiedpush.api.VariantType;
import org.jboss.aerogear.unifiedpush.dao.VariantDao;
import org.jboss.aerogear.unifiedpush.service.GenericVariantService;
import org.jboss.aerogear.unifiedpush.service.KeycloakService;
import org.jboss.aerogear.unifiedpush.service.annotations.LoggedIn;
import org.slf4j.Logger;

@Stateless
public class GenericVariantServiceImpl implements GenericVariantService {
	private static final Logger logger = org.slf4j.LoggerFactory.getLogger(GenericVariantServiceImpl.class);
	@Inject
	private KeycloakService keycloakService;

    @Inject
    private VariantDao variantDao;

    @Inject
    @LoggedIn
    private Instance<String> loginName;

    private final Map<String, Variant> variantIdFromClientIdCache = new ConcurrentHashMap<>();

    @Override
    public void addVariant(Variant variant) {
        variant.setDeveloper(loginName.get());
        variantDao.create(variant);
    }

    @Override
    public Variant findByVariantID(String variantID) {
        return variantDao.findByVariantID(variantID);
    }

    @Override
    public Variant findVariantByKeycloakClientID(String clientID) {
    	Variant variant = variantIdFromClientIdCache.get(clientID);
    	if (variant == null){
    		Iterable<String> clientVariants = keycloakService.getVariantIdsFromClient(clientID);

    		if(clientVariants != null){
        		for (String clientVariantId : clientVariants){
        			Variant clientVariant = findByVariantID(clientVariantId);
        			if (clientVariant != null && (clientVariant.getType() == VariantType.SIMPLE_PUSH)) {
        				// TODO - Support case of several Variants.
        				variant = clientVariant;
        				variantIdFromClientIdCache.put(clientID, variant);
        				break;
        			}
        		}
    		}
    	}

    	if (variant == null) {
    		logger.info("unable to resolve variant for clientID={}", clientID);
    	}

    	return variant;
    }

    @Override
    public void updateVariant(Variant variant) {
        variantDao.update(variant);
    }

    @Override
    public void removeVariant(Variant variant) {
        variantDao.delete(variant);
    }
}
