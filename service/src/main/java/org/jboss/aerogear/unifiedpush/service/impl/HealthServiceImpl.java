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

import org.jboss.aerogear.unifiedpush.jpa.dao.impl.JPAHealthDao;
import org.jboss.aerogear.unifiedpush.service.HealthService;
import org.jboss.aerogear.unifiedpush.service.impl.health.HealthDetails;
import org.jboss.aerogear.unifiedpush.service.impl.health.Ping;
import org.jboss.aerogear.unifiedpush.service.impl.health.PushNetwork;
import org.jboss.aerogear.unifiedpush.service.impl.health.Status;

import javax.ejb.AsyncResult;
import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

@Stateless
public class HealthServiceImpl implements HealthService {
    private static final PushNetwork[] PUSH_NETWORKS = new PushNetwork[] {
            new PushNetwork("Google Cloud Messaging", "android.googleapis.com", 80),
            new PushNetwork("Apple Push Network", "gateway.sandbox.push.apple.com", 2195),
            new PushNetwork("Windows Push Network", "db3.notify.windows.com", 443)
    };

    @Inject
    private JPAHealthDao healthDao;

    @Asynchronous
    @Override
    public Future<HealthDetails> dbStatus() {
        HealthDetails details = new HealthDetails();
        long current = System.currentTimeMillis();
        try {
            healthDao.dbCheck();
            details.setTest_status(Status.ok);
            details.setResult("database status is ok");
        } catch (Exception e) {
            details.setTest_status(Status.crit);
            details.setResult(e.getMessage());
        }
        details.setRuntime(System.currentTimeMillis() - current);
        return new AsyncResult<HealthDetails>(details);
    }

    @Asynchronous
    @Override
    public Future<List<HealthDetails>> networkStatus() {
        List<HealthDetails> results = new ArrayList<HealthDetails>(PUSH_NETWORKS.length);

        for (PushNetwork pushNetwork : PUSH_NETWORKS) {
            HealthDetails details = new HealthDetails();
            if (Ping.isReachable(pushNetwork.getHost(), pushNetwork.getPort())) {
                details.setTest_status(Status.ok);
                details.setDescription(pushNetwork.getName());
            } else {
                details.setResult("Could not connect");
                details.setDescription(String.format("Network not reachable '%s'", pushNetwork.getName()));
                details.setTest_status(Status.warn);
            }

            results.add(details);
        }

        return new AsyncResult<List<HealthDetails>>(results);
    }
}
