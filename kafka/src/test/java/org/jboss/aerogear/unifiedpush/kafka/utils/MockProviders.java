/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.unifiedpush.kafka.utils;

import static org.mockito.Mockito.mock;
import javax.enterprise.inject.Produces;
import javax.validation.Validator;

import org.jboss.aerogear.unifiedpush.service.ClientInstallationService;
import org.jboss.aerogear.unifiedpush.service.GenericVariantService;
import org.jboss.aerogear.unifiedpush.service.impl.SearchManager;
import org.jboss.aerogear.unifiedpush.service.metrics.PushMessageMetricsService;

/**
 * Container class for all used mock provider. They will be injected instead the real classes.
 */
public class MockProviders {

    private static PushMessageMetricsService pushMessageMetricsService = mock(PushMessageMetricsService.class);
    private ClientInstallationService clientInstallationService = mock(ClientInstallationService.class);
    private GenericVariantService genericVariantService = mock(GenericVariantService.class);
    private SearchManager searchManager = mock(SearchManager.class);
    private Validator validator = mock(Validator.class);

    @Produces
    public static PushMessageMetricsService getPushMessageMetricsService() {
        return pushMessageMetricsService;
    }

    @Produces
    public ClientInstallationService getClientInstallationService() {
        return clientInstallationService;
    }

    @Produces
    public GenericVariantService getGenericVariantService() {
        return genericVariantService;
    }

    @Produces
    public SearchManager getSearchManager() {
        return searchManager;
    }

    @Produces
    public Validator getValidator() {
        return validator;
    }
}
