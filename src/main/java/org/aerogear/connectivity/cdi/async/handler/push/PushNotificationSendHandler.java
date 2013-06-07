/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.aerogear.connectivity.cdi.async.handler.push;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.Asynchronous;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.aerogear.connectivity.cdi.event.BroadcastEvent;
import org.aerogear.connectivity.cdi.event.SelectedSendEvent;
import org.aerogear.connectivity.cdi.qualifier.Broadcast;
import org.aerogear.connectivity.cdi.qualifier.SelectedSend;
import org.aerogear.connectivity.rest.sender.messages.SelectiveSendMessage;
import org.aerogear.connectivity.service.SenderService;

public class PushNotificationSendHandler {

    @Inject
    private SenderService senderService;
    @Inject private Logger logger;

    @Asynchronous
    public void broadcast(@Observes @Broadcast BroadcastEvent broadcastEvent) {
        logger.log(Level.FINE, "Sending Broadcast Message");

        // send the message ...
        senderService.broadcast(broadcastEvent.getPushApplication(), broadcastEvent.getMessage());
    }

    @Asynchronous
    public void selectedSend(@Observes @SelectedSend SelectedSendEvent selectedSendEvent) {
        logger.log(Level.FINE, "Sending 'selected' Message");

        SelectiveSendMessage ssm = selectedSendEvent.getMessage();
        
        
        // send the message ...
        senderService.sendToAliases(
                selectedSendEvent.getPushApplication(),
                ssm);
    }
}
