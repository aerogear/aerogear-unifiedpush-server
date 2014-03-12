package org.jboss.aerogear.unifiedpush.model.jpa.validation;

import org.jboss.aerogear.unifiedpush.api.VariantType;
import org.jboss.aerogear.unifiedpush.model.jpa.InstallationEntity;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

/**
 *
 */
public class DeviceTokenValidator implements ConstraintValidator<DeviceTokenCheck, InstallationEntity> {
    private static final Pattern IOS_DEVICE_TOKEN = Pattern.compile("(?i)[a-z0-9]{64}");
    private static final Pattern ANDROID_DEVICE_TOKEN = Pattern.compile("(?i)[0-9a-z\\-_]{100,}");

    @Override
    public void initialize(DeviceTokenCheck constraintAnnotation) {
    }

    @Override
    public boolean isValid(InstallationEntity installation, ConstraintValidatorContext context) {
        final String deviceToken = installation.getDeviceToken();
        if (installation.getVariantType() == null || deviceToken == null) {
            return true;
        }

        final VariantType type = installation.getVariantType();

        switch (type) {
            case IOS:
                return IOS_DEVICE_TOKEN.matcher(deviceToken).matches();
            case ANDROID:
                return ANDROID_DEVICE_TOKEN.matcher(deviceToken).matches();
        }

        return true;
    }

}
