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
import org.jboss.aerogear.unifiedpush.api.iOSVariant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;
import javax.ejb.Singleton;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

@Singleton
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
                            logger.info("APNs connection for iOS Variant ({}) was inactive last 12 hours, disconnecting...", variantID);

                            final Future<Void> disconnectFuture = apnsClient.disconnect();

                            disconnectFuture.addListener(new GenericFutureListener<Future<? super Void>>() {
                                @Override
                                public void operationComplete(Future<? super Void> future) throws Exception {

                                    if (future.isSuccess()) {
                                        logger.debug("Disconnected from APNS due to inactive connection for iOS Variant ({})", variantID);
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

    public ApnsClient getApnsClientForVariant(final iOSVariant iOSVariant, final ServiceConstructor<ApnsClient> constructor) {
        final String connectionKey = extractConnectionKey(iOSVariant);
        ApnsClient client = apnsClientExpiringMap.get(connectionKey);

        if (client == null) {
            logger.debug("no cached connection for {}, establishing it", connectionKey);
            synchronized (apnsClientExpiringMap) {
                client = constructor.construct();

                if (client.isConnected()) {
                    putApnsClientForVariantID(connectionKey, client);
                }

                return client; // return the newly connected client
            }
        } else {
            logger.debug("reusing cached connection for {}", connectionKey);
            return client; // we had it already
        }
    }

    private String extractConnectionKey(final iOSVariant iOSVariant) {
        final StringBuilder sb = new StringBuilder()
                .append(iOSVariant.getVariantID())
                .append(iOSVariant.isProduction() ? "-prod" : "-dev");

        return  sb.toString();
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
        if (client.isConnected()) {
            client.disconnect().addListener(new ApnsDisconnectFutureListener());
        }
    }

    private class ApnsDisconnectFutureListener implements GenericFutureListener<Future<? super Void>> {
        @Override
        public void operationComplete(Future<? super Void> future) throws Exception {
            if (future.isSuccess()) {
                logger.debug("Successfully disconnected connection...");
            } else {
                final Throwable t = future.cause();
                logger.warn(t.getMessage(), t);
            }

        }
    }
}
