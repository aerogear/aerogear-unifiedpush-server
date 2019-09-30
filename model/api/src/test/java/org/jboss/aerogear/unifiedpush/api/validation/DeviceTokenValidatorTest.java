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
package org.jboss.aerogear.unifiedpush.api.validation;

import com.google.gson.Gson;
import org.jboss.aerogear.unifiedpush.api.VariantType;
import org.jboss.aerogear.unifiedpush.api.WebPushRegistration;
import org.junit.Test;

import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

public class DeviceTokenValidatorTest {

    @Test
    public void testValidToken() {
        final VariantType andVariantType = VariantType.ANDROID;
        assertThat(DeviceTokenValidator.isValidDeviceTokenForVariant("eHlfnI0__dI:APA91bEhtHefML2lr_sBQ-bdXIyEn5owzkZg_p_y7SRyNKRMZ3XuzZhBpTOYIh46tqRYQIc-7RTADk4nM5H-ONgPDWHodQDS24O5GuKP8EZEKwNh4Zxdv1wkZJh7cU2PoLz9gn4Nxqz-", andVariantType)).isTrue();
    }

    @Test
    public void testEmptyString() {
        final VariantType andVariantType = VariantType.ANDROID;
        assertThat(DeviceTokenValidator.isValidDeviceTokenForVariant("", andVariantType)).isFalse();
    }

    @Test
    public void testInvalidToken() {
        final VariantType andVariantType = VariantType.ANDROID;
        assertThat(DeviceTokenValidator.isValidDeviceTokenForVariant("some-bogus:token", andVariantType)).isFalse();
    }

    @Test
    public void testInvalidWebPush() {

        final Gson gson = new Gson();

        final VariantType webPushType = VariantType.WEB_PUSH;
        assertThat(DeviceTokenValidator.isValidDeviceTokenForVariant("some-bogus:token", webPushType)).isFalse();

        WebPushRegistration registration = new WebPushRegistration();
        String registrationJson = Base64.getEncoder().encodeToString(gson.toJson(registration).getBytes());
        assertThat(DeviceTokenValidator.isValidDeviceTokenForVariant(registrationJson, webPushType)).isFalse();

        registration.setEndpoint("https://some nonsense");
        registrationJson = gson.toJson(registration);
        assertThat(DeviceTokenValidator.isValidDeviceTokenForVariant(registrationJson, webPushType)).isFalse();

    }

    /**
     * This just tests that the endpoint and keyfields are set, it does not veryify anything.
     */
    @Test
    public void testValidWebPush() {

        final Gson gson = new Gson();

        final VariantType webPushType = VariantType.WEB_PUSH;

        WebPushRegistration registration = new WebPushRegistration();
        registration.setEndpoint("https://some nonsense");
        registration.getKeys().setAuth("authTokens");
        registration.getKeys().setP256dh("devicePublicKey");
        String registrationJson = Base64.getEncoder().encodeToString(gson.toJson(registration).getBytes());
        assertThat(DeviceTokenValidator.isValidDeviceTokenForVariant(registrationJson, webPushType)).isTrue();



    }
}
