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

package org.aerogear.connectivity.jms.sender;

import java.util.Map;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import org.aerogear.connectivity.model.PushApplication;
import org.aerogear.connectivity.service.PushApplicationService;
import org.aerogear.connectivity.service.SenderService;

@MessageDriven(name = "GlobalPushNotificationSenderListener", activationConfig = {
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Topic"),
        @ActivationConfigProperty(propertyName = "destination", propertyValue = "topic/aerogear/sender")
    })
public class GlobalPushNotificationSenderListener  implements MessageListener {
    
    @Inject
    private PushApplicationService pushApplicationService;
    @Inject
    private SenderService senderService;

    @Override
    public void onMessage(Message message) {
        
        ObjectMessage om = (ObjectMessage) message;
        
        String pushApplicationId;
        try {
            pushApplicationId = om.getStringProperty("pushApplicationID");
            PushApplication pushApp = pushApplicationService.findPushApplicationById(pushApplicationId);
            senderService.broadcast(pushApp, (Map<String, String>) om.getObject());
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
