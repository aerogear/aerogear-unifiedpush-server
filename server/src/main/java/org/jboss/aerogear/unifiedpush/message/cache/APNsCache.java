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
package org.jboss.aerogear.unifiedpush.message.cache;

import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;

import javax.enterprise.context.ApplicationScoped;

import org.jboss.aerogear.unifiedpush.model.iOSVariant;

import com.notnoop.apns.APNS;
import com.notnoop.apns.ApnsService;

@ApplicationScoped
public class APNsCache implements Serializable {
    private static final long serialVersionUID = -1913999384798892563L;

    private final ConcurrentHashMap<String, ApnsService> apnsCache = new ConcurrentHashMap<String, ApnsService>();

    public ApnsService getApnsServiceForVariant(iOSVariant iOSVariant) {
        ApnsService variantService = null;
        synchronized (apnsCache) {
            String variantId = iOSVariant.getId();
            variantService = apnsCache.get(variantId);

            if (variantService == null) {
                variantService = APNS
                        .newService()
                        .withCert(
                                new ByteArrayInputStream(iOSVariant.getCertificate()),
                                iOSVariant.getPassphrase()).withSandboxDestination()
                               .asQueued().build();

                // store it:
                apnsCache.put(variantId, variantService);
            }
        }

        return variantService;
    }
}
