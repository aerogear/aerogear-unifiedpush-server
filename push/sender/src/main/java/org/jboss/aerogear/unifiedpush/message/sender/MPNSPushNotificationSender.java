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
package org.jboss.aerogear.unifiedpush.message.sender;

import org.jboss.aerogear.unifiedpush.api.Variant;
import org.jboss.aerogear.unifiedpush.api.WindowsMPNSVariant;
import org.jboss.aerogear.unifiedpush.message.Message;
import org.jboss.aerogear.unifiedpush.message.UnifiedPushMessage;
import org.jboss.aerogear.unifiedpush.message.windows.Windows;
import org.jboss.aerogear.windows.mpns.MPNS;
import org.jboss.aerogear.windows.mpns.MpnsNotification;
import org.jboss.aerogear.windows.mpns.MpnsService;
import org.jboss.aerogear.windows.mpns.notifications.TileNotification;
import org.jboss.aerogear.windows.mpns.notifications.ToastNotification;

import java.util.Collection;
import java.util.List;

import static org.jboss.aerogear.unifiedpush.message.sender.WNSPushNotificationSender.createLaunchParam;

@SenderType(WindowsMPNSVariant.class)
public class MPNSPushNotificationSender implements PushNotificationSender {

    @Override
    public void sendPushMessage(Variant variant, Collection<String> clientIdentifiers, UnifiedPushMessage pushMessage, NotificationSenderCallback senderCallback) {
        // no need to send empty list
        if (clientIdentifiers.isEmpty()) {
            return;
        }

        MpnsService mpnsService = MPNS.newService().build();
        final Message message = pushMessage.getMessage();

        MpnsNotification notification;
        if (message.getWindows().getType() != null) {
            switch (message.getWindows().getType()) {
                case toast:
                    ToastNotification.Builder builder = MPNS.newNotification().toast()
                            .parameter(createLaunchParam(message.getWindows().getPage(), message.getAlert(), message.getUserData()))
                            .title(message.getAlert());
                    if (!message.getWindows().getTextFields().isEmpty()) {
                        builder.subtitle(message.getWindows().getTextFields().get(0));
                    }
                    notification = builder.build();
                    break;
                case badge:
                    notification = MPNS.newNotification().tile()
                            .count(message.getBadge()).build();
                    break;
                case raw:
                    notification = MPNS.newNotification().raw().body(message.getAlert()).build();
                    break;
                case tile:
                    Windows windows = message.getWindows();
                    TileNotification.Builder tile = MPNS.newNotification().tile();
                    tile.title(message.getAlert());

                    List<String> images = windows.getImages();
                    if (images.size() >= 1) {
                        tile.backgroundImage(images.get(0));
                    }

                    if (images.size() >= 2) {
                        tile.backBackgroundImage(images.get(1));
                    }

                    List<String> textFields = windows.getTextFields();
                    if (textFields.size() >= 1) {
                        tile.backTitle(textFields.get(0));
                    }
                    if (textFields.size() >= 2) {
                        tile.backContent(textFields.get(1));
                    }
                    notification = tile.build();
                    break;
                default:
                    senderCallback.onError("unknown type: " + message.getWindows().getType());
                    throw new IllegalArgumentException("unknown type: " + message.getWindows().getType());
            }
        } else {
            notification = MPNS.newNotification().toast()
                    .parameter(createLaunchParam(message.getWindows().getPage(), message.getAlert(), message.getUserData()))
                    .title(message.getAlert()).build();
        }

        for (String identifier : clientIdentifiers) {
            mpnsService.push(identifier, notification);
        }

        senderCallback.onSuccess();
    }
}
