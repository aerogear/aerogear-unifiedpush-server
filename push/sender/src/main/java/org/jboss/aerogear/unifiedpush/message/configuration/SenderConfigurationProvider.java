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
package org.jboss.aerogear.unifiedpush.message.configuration;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import org.jboss.aerogear.unifiedpush.api.VariantType;
import org.jboss.aerogear.unifiedpush.message.sender.SenderType;
import org.jboss.aerogear.unifiedpush.message.util.ConfigurationUtils;

/**
 * Loads and stores configuration for specific Push Networks.
 *
 * UPS provides sensible defaults specific to each Push Network and its limitations.
 *
 * Configuration can be changed by overriding system properties, e.g. via as VM args.
 *
 * Example: In order to change the configuration of {@link SenderConfiguration#batchSize()} for APNs (iOS) service,
 * one can provide following system property: <tt>-Daerogear.ios.batchSize=12000</tt>.
 *
 * The name of the network (<tt>aerogear.&lt;network&gt;.&lt;property&gt;</tt>) is specified by {@link VariantType#getTypeName()}.
 *
 * Look at {@link SenderConfiguration} for more details about available configurations.
 *
 * @see SenderConfiguration
 */
public class SenderConfigurationProvider {

    @Produces @ApplicationScoped @SenderType(VariantType.ANDROID)
    public SenderConfiguration produceAndroidConfiguration() {
        return loadConfigurationFor(VariantType.ANDROID, new SenderConfiguration(10, 1000));
    }

    @Produces @ApplicationScoped @SenderType(VariantType.ADM)
    public SenderConfiguration produceAdmConfiguration() {
        return loadConfigurationFor(VariantType.ADM, new SenderConfiguration(10, 1000));
    }

    @Produces @ApplicationScoped @SenderType(VariantType.IOS)
    public SenderConfiguration produceIosConfiguration() {
        return loadConfigurationFor(VariantType.IOS, new SenderConfiguration(3, 10000));
    }

    @Produces @ApplicationScoped @SenderType(VariantType.SIMPLE_PUSH)
    public SenderConfiguration produceSimplePushConfiguration() {
        return loadConfigurationFor(VariantType.SIMPLE_PUSH, new SenderConfiguration(10, 1000));
    }

    @Produces @ApplicationScoped @SenderType(VariantType.WINDOWS_MPNS)
    public SenderConfiguration produceWindowsMpnsConfiguration() {
        return loadConfigurationFor(VariantType.WINDOWS_MPNS, new SenderConfiguration(10, 1000));
    }

    @Produces @ApplicationScoped @SenderType(VariantType.WINDOWS_WNS)
    public SenderConfiguration produceWindowsWnsConfiguration() {
        return loadConfigurationFor(VariantType.WINDOWS_WNS, new SenderConfiguration(10, 1000));
    }

    private SenderConfiguration loadConfigurationFor(VariantType type, SenderConfiguration defaultConfiguration) {
        return new SenderConfiguration(
                getProperty(type, "batchesToLoad", defaultConfiguration.batchesToLoad(), Integer.class),
                getProperty(type, "batchSize", defaultConfiguration.batchSize(), Integer.class)
            );
    }

    @SuppressWarnings("unchecked")
    private <T> T getProperty(VariantType type, String property, T defaultValue, Class<T> expectedType) {
        String systemPropertyName = String.format("aerogear.%s.%s", type.getTypeName(), property);
        if (expectedType == String.class) {
            return (T) ConfigurationUtils.tryGetProperty(systemPropertyName, (String) defaultValue);
        } else if (expectedType == Integer.class) {
            return (T) ConfigurationUtils.tryGetIntegerProperty(systemPropertyName, (Integer) defaultValue);
        } else {
            throw new IllegalStateException("Unexpected type: " + expectedType);
        }
    }
}
