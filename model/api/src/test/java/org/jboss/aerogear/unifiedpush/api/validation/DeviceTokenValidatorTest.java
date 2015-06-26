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

import org.jboss.aerogear.unifiedpush.api.VariantType;
import org.junit.Test;

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
}
