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
package org.jboss.aerogear.unifiedpush.message.util;

import com.eatthepath.pushy.apns.ApnsClientBuilder;
import org.jboss.aerogear.unifiedpush.message.HealthNetworkService;
import org.jboss.aerogear.unifiedpush.message.sender.fcm.ConfigurableFCMSender;
import org.jboss.aerogear.unifiedpush.service.impl.health.HealthDetails;
import org.jboss.aerogear.unifiedpush.service.impl.health.Ping;
import org.jboss.aerogear.unifiedpush.service.impl.health.PushNetwork;
import org.jboss.aerogear.unifiedpush.service.impl.health.Status;

import javax.ejb.AsyncResult;
import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Future;

import static org.jboss.aerogear.unifiedpush.message.sender.apns.PushyApnsSender.CUSTOM_AEROGEAR_APNS_PUSH_HOST;
import static org.jboss.aerogear.unifiedpush.message.sender.apns.PushyApnsSender.CUSTOM_AEROGEAR_APNS_PUSH_PORT;
import static org.jboss.aerogear.unifiedpush.system.ConfigurationUtils.tryGetGlobalIntegerProperty;
import static org.jboss.aerogear.unifiedpush.system.ConfigurationUtils.tryGetGlobalProperty;

/**
 * Checks the health of the push networks.
 */
@Stateless
public class HealthNetworkServiceImpl implements HealthNetworkService {
    private static final String customAerogearApnsPushHost = tryGetGlobalProperty(CUSTOM_AEROGEAR_APNS_PUSH_HOST);
    private static final Integer customAerogearApnsPushPort = tryGetGlobalIntegerProperty(CUSTOM_AEROGEAR_APNS_PUSH_PORT);

    private static final String FCM_SEND_ENDPOINT = ConfigurableFCMSender.FCM_ENDPOINT_HOST.substring("https://".length(), ConfigurableFCMSender.FCM_ENDPOINT_HOST.indexOf('/', "https://".length()));
    public static final String WNS_SEND_ENDPOINT = "db3.notify.windows.com";
    private static final List<PushNetwork> PUSH_NETWORKS = new ArrayList<>(Arrays.asList(
            new PushNetwork[]{
                    new PushNetwork("Firebase Cloud Messaging", FCM_SEND_ENDPOINT, 443),
                    new PushNetwork("Apple Push Network Sandbox", ApnsClientBuilder.DEVELOPMENT_APNS_HOST, ApnsClientBuilder.DEFAULT_APNS_PORT),
                    new PushNetwork("Apple Push Network Production", ApnsClientBuilder.PRODUCTION_APNS_HOST, ApnsClientBuilder.DEFAULT_APNS_PORT),
                    new PushNetwork("Windows Push Network", WNS_SEND_ENDPOINT, 443)
            }
    ));

    static {
        if (customAerogearApnsPushHost != null) {
            final int port = customAerogearApnsPushPort != null ? customAerogearApnsPushPort : ApnsClientBuilder.DEFAULT_APNS_PORT;
            PUSH_NETWORKS.add(new PushNetwork("APNs Proxy host", customAerogearApnsPushHost, port));
        }
    }

    @Asynchronous
    @Override
    public Future<List<HealthDetails>> networkStatus() {
        final List<HealthDetails> results = new ArrayList<>(PUSH_NETWORKS.size());

        PUSH_NETWORKS.forEach(pushNetwork -> {
            HealthDetails details = new HealthDetails();
            details.start();
            details.setDescription(pushNetwork.getName());
            if (Ping.isReachable(pushNetwork.getHost(), pushNetwork.getPort())) {
                details.setTestStatus(Status.OK);
                details.setResult("online");
            } else {
                details.setResult(String.format("Network not reachable '%s'", pushNetwork.getName()));
                details.setTestStatus(Status.WARN);
            }

            results.add(details);
            details.stop();
        });

        return new AsyncResult<>(results);
    }
}
