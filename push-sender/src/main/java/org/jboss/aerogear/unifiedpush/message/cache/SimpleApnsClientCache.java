/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.unifiedpush.message.cache;

import com.eatthepath.pushy.apns.ApnsClient;
import net.jodah.expiringmap.ExpirationListener;
import net.jodah.expiringmap.ExpirationPolicy;
import net.jodah.expiringmap.ExpiringMap;
import org.jboss.aerogear.unifiedpush.api.APNSVariant;
import org.jboss.aerogear.unifiedpush.event.APNSVariantUpdateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;
import javax.ejb.Singleton;
import javax.enterprise.event.Observes;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

@Singleton
public class SimpleApnsClientCache {

    private static final Logger logger = LoggerFactory.getLogger(SimpleApnsClientCache.class);

    final ConcurrentMap<String, ApnsClient> apnsClientExpiringMap;

    {
        apnsClientExpiringMap = ExpiringMap.builder()

                // TODO: would be nice if it could be configured via System property:
                .expiration(12, TimeUnit.HOURS)
                .expirationPolicy(ExpirationPolicy.ACCESSED)
                .asyncExpirationListener((ExpirationListener<String, ApnsClient>) (variantID, apnsClient) -> {

                    if (apnsClient != null) {
                        logger.info("APNs connection for iOS Variant ({}) was inactive last 12 hours, disconnecting...", variantID);

                        final CompletableFuture<Void> disconnectFuture = apnsClient.close();

                        disconnectFuture.whenComplete((result, cause) -> {

                            if (result != null) {
                                logger.debug("Disconnected from APNS due to inactive connection for iOS Variant ({})", variantID);
                            } else {
                                final Throwable t = cause;
                                logger.warn(t.getMessage(), t);
                            }
                        });
                    }
                }).build();
    }

    public ApnsClient getApnsClientForVariant(final APNSVariant iOSVariant, final ServiceConstructor<ApnsClient> constructor) {
        final String connectionKey = extractConnectionKey(iOSVariant);
        ApnsClient client = apnsClientExpiringMap.get(connectionKey);

        if (client == null) {
            logger.debug("no cached connection for {}, establishing it", connectionKey);
            synchronized (apnsClientExpiringMap) {
                client = constructor.construct();

                if (client != null) {
                    putApnsClientForVariantID(connectionKey, client);
                }

                return client; // return the newly connected client
            }
        } else {
            logger.debug("reusing cached connection for {}", connectionKey);
            return client; // we had it already
        }
    }

    /**
     * Receives iOS variant change event to remove client from the cache and also tear down the connection.
     *
     * @param variant event fired when updating the variant
     */
    public void disconnectOnChange(final APNSVariant variant) {
        final String connectionKey = extractConnectionKey(variant);
        final ApnsClient client = apnsClientExpiringMap.remove(connectionKey);
        logger.debug("Removed client from cache for {}", variant.getVariantID());
        if (client != null) {
            tearDownApnsHttp2Connection(client);
        }
    }

    public void disconnectOnChangeEvent(@Observes final APNSVariantUpdateEvent APNSVariantUpdateEvent) {
        final APNSVariant variant = APNSVariantUpdateEvent.getApnsVariant();
        disconnectOnChange(variant);
    }

    private String extractConnectionKey(final APNSVariant iOSVariant) {
        final StringBuilder sb = new StringBuilder(iOSVariant.getVariantID())
                .append(iOSVariant.isProduction() ? "-prod" : "-dev");

        return sb.toString();
    }

    private void putApnsClientForVariantID(final String variantID, final ApnsClient apnsClient) {
        final ApnsClient client = apnsClientExpiringMap.putIfAbsent(variantID, apnsClient);
        if (client != null) {
            logger.warn("duplicate connection in pool, immediately shutting down the new connection");
            tearDownApnsHttp2Connection(apnsClient);  // we do not want this new connection
        }
    }

    @PreDestroy
    public void cleanUpConnection() {

        logger.debug("remove all connections before server shutdown");

        for (final Map.Entry<String, ApnsClient> cachedConnection : apnsClientExpiringMap.entrySet()) {

            final ApnsClient connection = cachedConnection.getValue();
            tearDownApnsHttp2Connection(connection);
        }
    }

    private void tearDownApnsHttp2Connection(final ApnsClient client) {
        if (client != null) {
            logger.trace("Tearing down connection to APNs for the given client");
            client.close().whenComplete((result, cause) -> {
                        if (result != null) {
                            logger.debug("Successfully disconnected connection...");
                        } else if (cause != null) {
                            final Throwable t = cause;
                            logger.warn(t.getMessage(), t);
                        }

                    }

            );
        }
    }

}