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

package org.aerogear.connectivity.jms;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import org.aerogear.connectivity.model.AndroidApplication;
import org.aerogear.connectivity.model.PushApplication;
import org.aerogear.connectivity.model.SimplePushApplication;
import org.aerogear.connectivity.model.iOSApplication;
import org.aerogear.connectivity.service.AndroidApplicationService;
import org.aerogear.connectivity.service.PushApplicationService;
import org.aerogear.connectivity.service.SimplePushApplicationService;
import org.aerogear.connectivity.service.iOSApplicationService;

@MessageDriven(name = "PushApplicationListener", activationConfig = {
    @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Topic"),
    @ActivationConfigProperty(propertyName = "destination", propertyValue = "topic/aerogear/pushApp")
})
public class PushApplicationListener implements MessageListener {
    
    
    @Inject
    private PushApplicationService pushAppService;
    @Inject
    private iOSApplicationService iOSappService;
    @Inject
    private AndroidApplicationService androidAppService;
    @Inject
    private SimplePushApplicationService simplePushApplicationService;

    
    

    @Override
    public void onMessage(Message message) {
        
        Object objectPayload = null;
        try {
            objectPayload = ((ObjectMessage)message).getObject();

            switch (message.getStringProperty("ApplicationType")) {
            case "aerogear.PushApplication":
                this.createPushApplication((PushApplication) objectPayload);
                break;

            case "aerogear.iOSApplication":
                this.createiOSApplication((iOSApplication) objectPayload, message.getStringProperty("PushApplicationID"));
                break;

            case "aerogear.AndroidApplication":
                this.createAndroidApplication((AndroidApplication) objectPayload, message.getStringProperty("PushApplicationID"));
                break;

            case "aerogear.SimplePushApplication":
                this.createSimplePushApplication((SimplePushApplication) objectPayload, message.getStringProperty("PushApplicationID"));
                break;

            default:
                // LOG WARNING....
                break;
            }
        } catch (JMSException e) {
            e.printStackTrace();
        }
        
    }
    
    
    private void createPushApplication(PushApplication pa) {
        pushAppService.addPushApplication(pa);
    }
    
    private void createiOSApplication(iOSApplication ios, String pushApplicationID) {
        
        // store the iOS variant:
        ios = iOSappService.addiOSApplication(ios);
        // find the root push app
        PushApplication pushApp = pushAppService.findPushApplicationById(pushApplicationID);
        // add iOS variant, and merge:
        pushAppService.addiOSApplication(pushApp, ios);
    }
    
    private void createAndroidApplication(AndroidApplication androidVariation, String pushApplicationID) {


        // store the Android variant:
        androidVariation = androidAppService.addAndroidApplication(androidVariation);
        // find the root push app
        PushApplication pushApp = pushAppService.findPushApplicationById(pushApplicationID);
        // add iOS variant, and merge:
        pushAppService.addAndroidApplication(pushApp, androidVariation);
    }
    
    private void createSimplePushApplication(SimplePushApplication spa, String pushApplicationID) {
        
        // store the SimplePush variant:
        spa = simplePushApplicationService.addSimplePushApplication(spa);
        // find the root push app
        PushApplication pushApp = pushAppService.findPushApplicationById(pushApplicationID);
        // add iOS variant, and merge:
        pushAppService.addSimplePushApplication(pushApp, spa);
        
    }
}
