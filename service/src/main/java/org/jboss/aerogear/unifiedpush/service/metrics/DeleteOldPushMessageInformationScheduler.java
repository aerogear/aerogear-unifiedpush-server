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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.inject.Inject;

@Singleton
public class DeleteOldPushMessageInformationScheduler {

    private static final Logger logger = LoggerFactory.getLogger(DeleteOldPushMessageInformationScheduler.class);

    @Inject
    private PushMessageMetricsService service;

    /**
     * Job that triggers a delete of outdated metric information from the Server.
     *
     * Note: Occurring every day at midnight in the default time zone associated with the container
     * in which the application is executing. These are the default values from the @Schedule annotation.
     */
    @Schedule
    public void deleteOutdatedFlatMetrics(){
        logger.trace("scheduled deletion for outdated push info data");
        service.deleteOutdatedFlatPushInformationData();
    }
}
