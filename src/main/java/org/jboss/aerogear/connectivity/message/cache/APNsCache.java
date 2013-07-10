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
package org.jboss.aerogear.connectivity.message.cache;

import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;

import javax.enterprise.context.ApplicationScoped;

import org.jboss.aerogear.connectivity.model.iOSVariant;

import com.notnoop.apns.APNS;
import com.notnoop.apns.ApnsService;

@ApplicationScoped
public class APNsCache implements Serializable {
    private static final long serialVersionUID = -1913999384798892563L;

    private final ConcurrentHashMap<String, ApnsService> developmentCache = new ConcurrentHashMap<String, ApnsService>();
    private final ConcurrentHashMap<String, ApnsService> productionCache  = new ConcurrentHashMap<String, ApnsService>();

    public ApnsService getDevelopmentService(iOSVariant iOSVariant) {
        ApnsService apnsService = null;
        synchronized (developmentCache) {
            String variantId = iOSVariant.getId();
            apnsService = developmentCache.get(variantId);

            if (apnsService == null) {
                apnsService = APNS
                        .newService()
                        .withCert(
                                new ByteArrayInputStream(iOSVariant.getDevelopmentCertificate()),
                                iOSVariant.getDevelopmentPassphrase()).withSandboxDestination()
                               .asQueued().build();

                // store it:
                developmentCache.put(variantId, apnsService);
            }
        }

        return apnsService;
    }

    public ApnsService getProductionService(iOSVariant iOSVariant) {
        ApnsService apnsService = null;
        synchronized (productionCache) {
            String variantId = iOSVariant.getId();
            apnsService = productionCache.get(variantId);

            if (apnsService == null) {
                apnsService = APNS
                        .newService()
                        .withCert(
                                new ByteArrayInputStream(iOSVariant.getProductionCertificate()),
                                iOSVariant.getProductionPassphrase()).withProductionDestination()
                               .asQueued().build();

                // store it:
                productionCache.put(variantId, apnsService);
            }
        }

        return apnsService;
    }

}
