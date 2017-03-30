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

import com.relayrides.pushy.apns.ApnsClient;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.jodah.expiringmap.ExpirationListener;
import net.jodah.expiringmap.ExpirationPolicy;
import net.jodah.expiringmap.ExpiringMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

@ApplicationScoped
public class SimpleApnsClientCache {

    private final Logger logger = LoggerFactory.getLogger(SimpleApnsClientCache.class);

    final ConcurrentMap<String, ApnsClient> apnsClientExpiringMap;
    {
        apnsClientExpiringMap = ExpiringMap.builder()

                // TODO: would be nice if it could be configured via System property:
                .expiration(12, TimeUnit.HOURS)
                .expirationPolicy(ExpirationPolicy.ACCESSED)
                .asyncExpirationListener(new ExpirationListener<String, ApnsClient>() {

                    @Override
                    public void expired(final String variantID, final ApnsClient apnsClient) {

                        if (apnsClient.isConnected()) {
                            logger.info(String.format("APNs connection for iOS Variant (%s) was inactive last 12 hours, disconnecting...", variantID));

                            final Future<Void> disconnectFuture = apnsClient.disconnect();

                            disconnectFuture.addListener(new GenericFutureListener<Future<? super Void>>() {
                                @Override
                                public void operationComplete(Future<? super Void> future) throws Exception {

                                    if (future.isSuccess()) {
                                        logger.debug(String.format("Disconnected from APNS due to inactive connection for iOS Variant (%s)", variantID));
                                    } else {
                                        final Throwable t = future.cause();
                                        logger.warn(t.getMessage(), t);
                                    }
                                }
                            });
                        }
                    }
                }).build();
    }

    public ApnsClient getApnsClientForVariantID(final String variantID) {
        return apnsClientExpiringMap.get(variantID);
    }

    public void putApnsClientForVariantID(final String variantID, final ApnsClient apnsClient) {
        apnsClientExpiringMap.put(variantID, apnsClient);
    }

    @PreDestroy
    public void cleanUpConnection() {

        logger.debug("remove all connections before server shutdown");

        for (final Map.Entry<String, ApnsClient> cachedConnection : apnsClientExpiringMap.entrySet()) {

            final ApnsClient connection = cachedConnection.getValue();
            if (connection.isConnected()) {
                connection.disconnect().addListener(new GenericFutureListener<Future<? super Void>>() {
                    @Override
                    public void operationComplete(Future<? super Void> future) throws Exception {

                        if (future.isSuccess()) {
                            logger.debug(String.format("Successfully disconnected connection for iOS variant %s", cachedConnection.getKey()));
                        } else {
                            final Throwable t = future.cause();
                            logger.warn(t.getMessage(), t);
                        }
                    }
                });
            }
        }
    }
}
