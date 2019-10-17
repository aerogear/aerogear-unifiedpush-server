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
package org.jboss.aerogear.unifiedpush.api.validation;

import com.google.gson.Gson;
import org.jboss.aerogear.unifiedpush.api.Installation;
import org.jboss.aerogear.unifiedpush.api.VariantType;
import org.jboss.aerogear.unifiedpush.api.WebPushRegistration;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Base64;
import java.util.regex.Pattern;

/**
 * Validator that will validate if the device token matches to {@code VariantType} specific pattern.
 * @see VariantType
 */
public class DeviceTokenValidator implements ConstraintValidator<DeviceTokenCheck, Installation> {
    /**
     * Pattern for iOS is pretty well defined as the library we use for sending assumes HEX.
     * @see <a href="https://github.com/notnoop/java-apns/blob/20c10ebd22e15a55c0c1c12695c535d37435dcfd/src/main/java/com/notnoop/apns/internal/Utilities.java#L114">notnoop apns</a>
     */
    private static final Pattern IOS_DEVICE_TOKEN = Pattern.compile("(?i)[a-f0-9 -]{64,}");
    /**
     * Pattern for android is harder to define that is why we kept it lenient it is at least 100 characters long and can
     * consist of digits, alphas, - and _ all have one of these separators
     */
    private static final Pattern ANDROID_DEVICE_TOKEN = Pattern.compile("(?i)[0-9a-z\\-_:]{100,}");


    @Override
    public void initialize(DeviceTokenCheck constraintAnnotation) {
    }

    @Override
    public boolean isValid(Installation installation, ConstraintValidatorContext context) {
        final String deviceToken = installation.getDeviceToken();
        if (installation.getVariant() == null || installation.getVariant().getType() == null || deviceToken == null) {
            return false;
        }
        final VariantType type = installation.getVariant().getType();

        return isValidDeviceTokenForVariant(deviceToken, type);
    }


    /**
     * Helper to run quick up-front validations.
     *
     * @param deviceToken the submitted device token
     * @param type type of the variant
     * @return true if the token is valid
     */
    public static boolean isValidDeviceTokenForVariant(final String deviceToken, final VariantType type) {
        switch (type) {
            case IOS:
            case IOS_TOKEN:
                return IOS_DEVICE_TOKEN.matcher(deviceToken).matches();
            case ANDROID:
                return ANDROID_DEVICE_TOKEN.matcher(deviceToken).matches();
            case WEB_PUSH:
                return isWebPushRegistration(deviceToken);
        }
        return false;
    }

    private static boolean isWebPushRegistration(String deviceToken) {
        try {
            String jsonToken = new String(Base64.getDecoder().decode(deviceToken));
            Gson gson = new Gson();
            WebPushRegistration registration = gson.fromJson(jsonToken, WebPushRegistration.class);
            return !registration.getEndpoint().isEmpty()
                    && !registration.getKeys().getAuth().isEmpty()
                    && !registration.getKeys().getP256dh().isEmpty();
        } catch (Exception ignore) {
            return false;
        }
    }
}
