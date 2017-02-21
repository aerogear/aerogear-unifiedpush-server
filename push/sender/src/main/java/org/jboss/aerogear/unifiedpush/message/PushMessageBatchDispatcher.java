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
package org.jboss.aerogear.unifiedpush.message;

import org.jboss.aerogear.unifiedpush.api.PushApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.List;

@Stateless
public class PushMessageBatchDispatcher {

    private final Logger logger = LoggerFactory.getLogger(PushMessageBatchDispatcher.class);

    @Inject
    private NotificationRouter notificationRouter;

    /**
     * Helper method, fired by the endpoint, to allow async processing of large push message collections
     * @param pushApplication push application receiving the message
     * @param messages collection of messages for the given push application
     */
    @Asynchronous
    public void dispatchPushMessageBatch(final PushApplication pushApplication, final List<InternalUnifiedPushMessage> messages) {

        for (InternalUnifiedPushMessage msg : messages) {
            // submitted to EJB:
            notificationRouter.submit(pushApplication, msg);
            logger.debug(String.format("Push Message Request from [%s] API was internally submitted for further processing", msg.getClientIdentifier()));
        }
    }

}
