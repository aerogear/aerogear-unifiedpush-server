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
package org.jboss.aerogear.unifiedpush.service;

import org.jboss.aerogear.unifiedpush.service.impl.health.HealthDetails;

import java.util.concurrent.Future;

/**
 * Service query database to see how how healthy we are.
 */
public interface HealthDBService {
    /**
     * Get the database status and return a HealthDetails.
     * If the database is reachable and functioning Status.OK otherwise Status.CRIT
     * @see org.jboss.aerogear.unifiedpush.service.impl.health.Status
     * @return the HealthDetails with the current database health
     */
    Future<HealthDetails> dbStatus();
}
