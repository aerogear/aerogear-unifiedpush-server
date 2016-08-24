/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
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
package org.jboss.aerogear.unifiedpush.message.sender;

import ar.com.fernandospr.wns.WnsService;
import ar.com.fernandospr.wns.exceptions.WnsException;
import ar.com.fernandospr.wns.model.WnsBadge;
import ar.com.fernandospr.wns.model.WnsNotificationRequestOptional;
import ar.com.fernandospr.wns.model.WnsNotificationResponse;
import ar.com.fernandospr.wns.model.WnsRaw;
import ar.com.fernandospr.wns.model.WnsTile;
import ar.com.fernandospr.wns.model.WnsToast;
import ar.com.fernandospr.wns.model.builders.WnsAbstractBuilder;
import ar.com.fernandospr.wns.model.builders.WnsBadgeBuilder;
import ar.com.fernandospr.wns.model.builders.WnsRawBuilder;
import ar.com.fernandospr.wns.model.builders.WnsTileBuilder;
import ar.com.fernandospr.wns.model.builders.WnsToastBuilder;
import org.jboss.aerogear.unifiedpush.api.Variant;
import org.jboss.aerogear.unifiedpush.api.VariantType;
import org.jboss.aerogear.unifiedpush.api.WindowsWNSVariant;
import org.jboss.aerogear.unifiedpush.message.InternalUnifiedPushMessage;
import org.jboss.aerogear.unifiedpush.message.Message;
import org.jboss.aerogear.unifiedpush.message.UnifiedPushMessage;
import org.jboss.aerogear.unifiedpush.message.windows.Windows;
import org.jboss.aerogear.unifiedpush.service.ClientInstallationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.UriBuilder;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@SenderType(VariantType.WINDOWS_WNS)
public class WNSPushNotificationSender implements PushNotificationSender {

    private final Logger logger = LoggerFactory.getLogger(WNSPushNotificationSender.class);

    private static final String CORDOVA = "cordova";
    static final String CORDOVA_PAGE = "/Plugins/aerogear-cordova-push/P.xaml";
    private String pushMessageInformationId;

    @Inject
    private ClientInstallationService clientInstallationService;

    @Override
    public void sendPushMessage(Variant variant, Collection<String> clientIdentifiers, UnifiedPushMessage pushMessage, String pushMessageInformationId, NotificationSenderCallback senderCallback) {
        setPushMessageInformationId(pushMessageInformationId);

        // no need to send empty list
        if (clientIdentifiers.isEmpty()) {
            return;
        }

        final WindowsWNSVariant windowsVariant = (WindowsWNSVariant) variant;
        WnsService wnsService = new WnsService(windowsVariant.getSid(), windowsVariant.getClientSecret(), false);

        Set<String> expiredClientIdentifiers = new HashSet<>(clientIdentifiers.size());
        ArrayList<String> channelUris = new ArrayList<>(clientIdentifiers);
        Message message = pushMessage.getMessage();
        try {
            WnsNotificationRequestOptional optional = new WnsNotificationRequestOptional();
            int ttl = pushMessage.getConfig().getTimeToLive();
            if (ttl != -1) {
                optional.ttl = String.valueOf(ttl);
            }

            final List<WnsNotificationResponse> responses;
            if (message.getWindows().getType() != null) {
                switch (message.getWindows().getType()) {
                    case toast:
                        responses = wnsService.pushToast(channelUris, optional, createToastMessage(message));
                        break;
                    case badge:
                        responses = wnsService.pushBadge(channelUris, optional, createBadgeMessage(message));
                        break;
                    case raw:
                        responses = wnsService.pushRaw(channelUris, optional, createRawMessage(message));
                        break;
                    case tile:
                        responses = wnsService.pushTile(channelUris, optional, createTileMessage(message));
                        break;
                    default:
                        throw new IllegalArgumentException("unknown type: " + message.getWindows().getType());
                }
            } else {
                responses = wnsService.pushToast(channelUris, optional, createSimpleToastMessage(message));
            }

            logger.info(String.format("Sent push notification to WNS for %d  tokens", channelUris.size()));

            expiredClientIdentifiers.addAll(responses.stream().filter(response -> response.code == HttpServletResponse.SC_GONE).map(response -> response.channelUri).collect(Collectors.toList()));
            if (!expiredClientIdentifiers.isEmpty()) {
                logger.info(String.format("Deleting '%d' expired WNS installations", expiredClientIdentifiers.size()));
                clientInstallationService.removeInstallationsForVariantByDeviceTokens(variant.getVariantID(), expiredClientIdentifiers);
            }
            logger.debug("Message to WNS has been submitted");
            senderCallback.onSuccess();
        } catch (WnsException | IllegalArgumentException exception) {
            senderCallback.onError(exception.getMessage());
        }
    }

    WnsToast createToastMessage(Message message) {
        final WnsToastBuilder builder = new WnsToastBuilder();
        Windows windows = message.getWindows();
        if (windows.getDuration() != null) {
            builder.duration(windows.getDuration().toString());
        }
        builder.audioSrc(message.getSound());
        builder.launch(createLaunchParam(message.getWindows().getPage(), message.getAlert(), message.getUserData(), getPushMessageInformationId()));
        createMessage(message, windows.getToastType().toString(), builder);
        return builder.build();
    }

    private static WnsBadge createBadgeMessage(Message message) {
        final WnsBadgeBuilder builder = new WnsBadgeBuilder();
        Windows windows = message.getWindows();
        if (windows.getBadge() != null) {
            builder.value(windows.getBadge().toString());
        } else {
            builder.value(message.getBadge());
        }
        return builder.build();
    }

    private static WnsRaw createRawMessage(Message message) {
        final WnsRawBuilder builder = new WnsRawBuilder();
        builder.stream(message.getAlert().getBytes());
        return builder.build();
    }

    WnsTile createTileMessage(Message message) {
        final WnsTileBuilder builder = new WnsTileBuilder();
        createMessage(message, message.getWindows().getTileType().toString(), builder);
        return builder.build();
    }

    private static void createMessage(Message message, String type, WnsAbstractBuilder builder) {
        Windows windows = message.getWindows();
        List<String> param = new ArrayList<>(windows.getImages());
        param.add(message.getAlert());
        param.addAll(windows.getTextFields());

        createTemplate(builder, type, param);
    }

    private static void createTemplate(WnsAbstractBuilder builder, String type, List<String> param) {
        try {
            Method[] methods = builder.getClass().getMethods();
            for (Method method : methods) {
                if (method.getName().equals("bindingTemplate" + type)) {
                    int methodArgs = method.getParameterTypes().length;
                    if (methodArgs == param.size()) {
                        method.invoke(builder, param.toArray(new String[param.size()]));
                    } else {
                        throw new IllegalArgumentException("this template needs " + methodArgs
                                + " fields, but you specified " + param.size());
                    }
                }
            }
        } catch (InvocationTargetException e) {
            throw new RuntimeException("error thrown while invoking template build method: " + type, e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("something wrong with the permissions of this method", e);
        }
    }

    WnsToast createSimpleToastMessage(Message message) {
        final WnsToastBuilder builder = new WnsToastBuilder().bindingTemplateToastText01(message.getAlert());
        final Map<String, Object> data = message.getUserData();
        builder.launch(createLaunchParam(message.getWindows().getPage(), message.getAlert(), data, getPushMessageInformationId()));
        return builder.build();
    }

    public String getPushMessageInformationId() {
        return pushMessageInformationId;
    }

    public void setPushMessageInformationId(String pushMessageInformationId) {
        this.pushMessageInformationId = pushMessageInformationId;
    }

    static String createLaunchParam(String page, String message, Map<String, Object> data, String pushMessageInformationId) {
        final UriBuilder uriBuilder = UriBuilder.fromPath("");

        data.forEach(uriBuilder::queryParam);
        if (message != null) {
            uriBuilder.queryParam("message", message);
        }
        //add aerogear-push-id
        uriBuilder.queryParam(InternalUnifiedPushMessage.PUSH_MESSAGE_ID, pushMessageInformationId);
        final String query = uriBuilder.build().getQuery();
        return (CORDOVA.equals(page) ? CORDOVA_PAGE : page) + (query != null ? ("?" + query) : "");
    }
}
