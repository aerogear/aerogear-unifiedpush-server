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
package org.jboss.aerogear.unifiedpush.service.metrics;

import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.inject.Inject;

@Singleton
public class DeleteOldPushMessageInformationScheduler {

    @Inject
    private PushMessageMetricsService service;

    /**
     * Job that triggers a delete of outdated metric information.
     *
     * Occurring every day at midnight in the default time zone associated with the container
     * in which the application is executing.
     */
    @Schedule
    public void deleteOutdatedMetrics(){
        service.deleteOutdatePushInformationData();
    }
}
