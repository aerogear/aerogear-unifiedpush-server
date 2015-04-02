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

import com.google.android.gcm.server.Constants;
import com.notnoop.apns.internal.Utilities;
import org.jboss.aerogear.unifiedpush.message.HealthNetworkService;
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

import static org.jboss.aerogear.unifiedpush.message.sender.APNsPushNotificationSender.CUSTOM_AEROGEAR_APNS_PUSH_HOST;
import static org.jboss.aerogear.unifiedpush.message.sender.APNsPushNotificationSender.CUSTOM_AEROGEAR_APNS_PUSH_PORT;
import static org.jboss.aerogear.unifiedpush.message.util.ConfigurationUtils.tryGetIntegerProperty;
import static org.jboss.aerogear.unifiedpush.message.util.ConfigurationUtils.tryGetProperty;

/**
 * Checks the health of the push networks.
 */
@Stateless
public class HealthNetworkServiceImpl implements HealthNetworkService {
    private static final String customAerogearApnsPushHost = tryGetProperty(CUSTOM_AEROGEAR_APNS_PUSH_HOST);
    private static final Integer customAerogearApnsPushPort = tryGetIntegerProperty(CUSTOM_AEROGEAR_APNS_PUSH_PORT);

    private static final String GCM_SEND_ENDPOINT = Constants.GCM_SEND_ENDPOINT.substring("https://".length(), Constants.GCM_SEND_ENDPOINT.indexOf('/', "https://".length()));
    public static final String WNS_SEND_ENDPOINT = "db3.notify.windows.com";
    private static final List<PushNetwork> PUSH_NETWORKS = new ArrayList<PushNetwork>(Arrays.asList(
            new PushNetwork[]{
                    new PushNetwork("Google Cloud Messaging", GCM_SEND_ENDPOINT, 443),
                    new PushNetwork("Apple Push Network Sandbox", Utilities.SANDBOX_GATEWAY_HOST, Utilities.SANDBOX_GATEWAY_PORT),
                    new PushNetwork("Apple Push Network Production", Utilities.PRODUCTION_GATEWAY_HOST, Utilities.PRODUCTION_GATEWAY_PORT),
                    new PushNetwork("Windows Push Network", WNS_SEND_ENDPOINT, 443)
            }
    ));

    static {
        if (customAerogearApnsPushHost != null) {
            final int port = customAerogearApnsPushPort != null ? customAerogearApnsPushPort : Utilities.SANDBOX_GATEWAY_PORT;
            PUSH_NETWORKS.add(new PushNetwork("Proxy host", customAerogearApnsPushHost, port));
        }
    }

    @Asynchronous
    @Override
    public Future<List<HealthDetails>> networkStatus() {
        List<HealthDetails> results = new ArrayList<HealthDetails>(PUSH_NETWORKS.size());

        for (PushNetwork pushNetwork : PUSH_NETWORKS) {
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
        }

        return new AsyncResult<List<HealthDetails>>(results);
    }
}
