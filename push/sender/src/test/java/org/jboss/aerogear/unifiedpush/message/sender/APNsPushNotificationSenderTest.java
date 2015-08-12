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


import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.notnoop.apns.ApnsService;
import org.jboss.aerogear.unifiedpush.api.iOSVariant;
import org.jboss.aerogear.unifiedpush.message.UnifiedPushMessage;
import org.jboss.aerogear.unifiedpush.message.cache.AbstractServiceCache.ServiceConstructor;
import org.jboss.aerogear.unifiedpush.message.cache.ApnsServiceCache;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class APNsPushNotificationSenderTest {

    @Test
    public void callbackOnError() throws Exception {
        final APNsPushNotificationSender sender = new APNsPushNotificationSender(new ApnsServiceCache());
        final NotificationSenderCallback callback = mock(NotificationSenderCallback.class);

        final iOSVariant iosVariant = mock(iOSVariant.class);
        when(iosVariant.getVariantID()).thenReturn(UUID.randomUUID().toString());
        when(iosVariant.getCertificate()).thenReturn(readCertificate());
        when(iosVariant.getPassphrase()).thenReturn("123456");

        sender.sendPushMessage(iosVariant, Arrays.asList("token"), new UnifiedPushMessage(), "123", callback);

        verify(callback).onError("Error sending payload to APNs server: Invalid hex character: t");
    }

    @Test
    public void noBadge() throws Exception {
        final ApnsService apnsService = mock(ApnsService.class);
        final APNsPushNotificationSender sender = mockSender(apnsService);
        final List<String> tokens = Collections.singletonList("token");

        sender.sendPushMessage(iosVariant(), tokens, new UnifiedPushMessage(), "123", mockCallback());

        final ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(apnsService).push(eq(tokens), captor.capture(), any(Date.class));
        assertFalse("JSON Message should NOT contain a badge property.", captor.getValue().contains("\"badge\":0"));
    }

    @Test
    public void badgeZero() throws Exception {
        final ApnsService apnsService = mock(ApnsService.class);
        final APNsPushNotificationSender sender = mockSender(apnsService);
        final List<String> tokens = Collections.singletonList("token");
        final UnifiedPushMessage unifiedPushMessage = new UnifiedPushMessage();
        unifiedPushMessage.getMessage().setBadge(0);

        sender.sendPushMessage(iosVariant(), tokens, unifiedPushMessage, "123", mockCallback());

        final ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(apnsService).push(eq(tokens), captor.capture(), any(Date.class));
        assertTrue("JSON Message should contain a badge property.", captor.getValue().contains("\"badge\":0"));
    }

    @Test
    public void badgeGreaterThanZero() throws Exception {
        final ApnsService apnsService = mock(ApnsService.class);
        final APNsPushNotificationSender sender = mockSender(apnsService);
        final List<String> tokens = Collections.singletonList("token");
        final UnifiedPushMessage unifiedPushMessage = new UnifiedPushMessage();
        unifiedPushMessage.getMessage().setBadge(9);

        sender.sendPushMessage(iosVariant(), tokens, unifiedPushMessage, "123", mockCallback());

        final ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(apnsService).push(eq(tokens), captor.capture(), any(Date.class));
        assertTrue("JSON Message should contain a badge property with value 9: ", captor.getValue().contains("\"badge\":9"));
    }

    private iOSVariant iosVariant() throws Exception {
        final iOSVariant iosVariant = mock(iOSVariant.class);
        when(iosVariant.getVariantID()).thenReturn(UUID.randomUUID().toString());
        when(iosVariant.getCertificate()).thenReturn(readCertificate());
        when(iosVariant.getPassphrase()).thenReturn("123456");
        return iosVariant;
    }

    private NotificationSenderCallback mockCallback() {
        return mock(NotificationSenderCallback.class);
    }

    private APNsPushNotificationSender mockSender(final ApnsService apnsService) {
        final ApnsServiceCache serviceCache = mock(ApnsServiceCache.class);
        when(serviceCache.dequeueOrCreateNewService(anyString(),
                anyString(),
                any(ServiceConstructor.class))).thenReturn(apnsService);
        final APNsPushNotificationSender sender = new APNsPushNotificationSender(serviceCache);
        return sender;
    }

    /**
     * The store read by this method was copied from
     * https://github.com/notnoop/java-apns/tree/master/src/test/resources
     */
    private static byte[] readCertificate() throws Exception {
        return asByteArray(APNsPushNotificationSenderTest.class.getResourceAsStream("/clientStore.p12"));
    }

    private static byte[] asByteArray(final InputStream is) throws IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int reads = is.read();
        while (reads != -1) {
            baos.write(reads);
            reads = is.read();
        }
        return baos.toByteArray();
    }

}
