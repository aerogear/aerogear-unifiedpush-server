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
package org.jboss.aerogear.unifiedpush.service.dashboard;

/**
 * Container for all statistic numbers for the current user, presented on the 'Dashboard'
 */
public class DashboardData {

    private long applications;
    private long devices;
    private long messages;

    /**
     * Number of push applications of the current user.
     *
     * @return number of users
     */
    public long getApplications() {
        return applications;
    }

    public void setApplications(long applications) {
        this.applications = applications;
    }

    /**
     * Number of registered devices for the push applications of the current user.
     *
     * @return number of devices
     */
    public long getDevices() {
        return devices;
    }

    public void setDevices(long devices) {
        this.devices = devices;
    }

    /**
     * Number of sent messages from push applications of the current user.
     *
     * @return number of messages
     */
    public long getMessages() {
        return messages;
    }

    public void setMessages(long messages) {
        this.messages = messages;
    }
}
