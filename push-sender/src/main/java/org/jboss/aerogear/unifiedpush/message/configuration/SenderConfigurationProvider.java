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

import org.jboss.aerogear.unifiedpush.api.VariantType;
import org.jboss.aerogear.unifiedpush.message.sender.SenderType;
import org.jboss.aerogear.unifiedpush.system.ConfigurationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

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

    private static final Logger logger = LoggerFactory.getLogger(SenderConfigurationProvider.class);

    @Produces @ApplicationScoped @SenderType(VariantType.ANDROID)
    public SenderConfiguration produceAndroidConfiguration() {
        return loadConfigurationFor(VariantType.ANDROID, new SenderConfiguration(10, 1000));
    }

    @Produces @ApplicationScoped @SenderType(VariantType.IOS)
    public SenderConfiguration produceIosConfiguration() {
        return loadConfigurationFor(VariantType.IOS, new SenderConfiguration(3, 2000));
    }

    @Produces @ApplicationScoped @SenderType(VariantType.IOS_TOKEN)
    public SenderConfiguration produceIosTokenConfiguration() {
        return loadConfigurationFor(VariantType.IOS_TOKEN, new SenderConfiguration(3, 2000));
    }

    @Produces @ApplicationScoped @SenderType(VariantType.WEB_PUSH)
    public SenderConfiguration produceWebPushConfiguration() {
        return loadConfigurationFor(VariantType.WEB_PUSH, new SenderConfiguration(10, 1000));
    }

    private SenderConfiguration loadConfigurationFor(VariantType type, SenderConfiguration defaultConfiguration) {
        return validateAndSanitizeConfiguration(type, new SenderConfiguration(
                getProperty(type, ConfigurationProperty.batchesToLoad, defaultConfiguration.batchesToLoad(), Integer.class),
                getProperty(type, ConfigurationProperty.batchSize, defaultConfiguration.batchSize(), Integer.class)
            ));
    }

    /**
     * Validates that configuration is correct with regards to push networks limitations or implementation, etc.
     */
    private SenderConfiguration validateAndSanitizeConfiguration(VariantType type, SenderConfiguration configuration) {
        switch (type) {
            case ANDROID:
                if (configuration.batchSize() > 1000) {
                    logger.warn(String
                            .format("Sender configuration -D%s=%s is invalid: at most 1000 tokens can be submitted to GCM in one batch",
                                    getSystemPropertyName(type, ConfigurationProperty.batchSize), configuration.batchSize()));
                    configuration.setBatchSize(1000);
                }
                break;
            default:
                break;
        }
        return configuration;
    }

    @SuppressWarnings("unchecked")
    private static <T> T getProperty(VariantType type, ConfigurationProperty property, T defaultValue,
            Class<T> expectedType) {
        String systemPropertyName = getSystemPropertyName(type, property);
        if (expectedType == String.class) {
            return (T) ConfigurationUtils.tryGetGlobalProperty(systemPropertyName, (String) defaultValue);
        } else if (expectedType == Integer.class) {
            return (T) ConfigurationUtils.tryGetGlobalIntegerProperty(systemPropertyName, (Integer) defaultValue);
        } else {
            throw new IllegalStateException("Unexpected type: " + expectedType);
        }
    }

    private static String getSystemPropertyName(VariantType type, ConfigurationProperty property) {
        return String.format("aerogear.%s.%s", type.getTypeName(), property.toString());
    }

    /**
     * Configuration properties are matching the properties of {@link SenderConfiguration} fields / accessors.
     * The enum members intentionally use camel-case to avoid need for name conversion.
     */
    private enum ConfigurationProperty {
        batchesToLoad,
        batchSize
    }
}
