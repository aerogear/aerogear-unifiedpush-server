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
package org.jboss.aerogear.unifiedpush.message;

/**
 * This is {@link UnifiedPushMessage} with some additional information, which we will use for internal purposes,
 * like analytics tracking.
 */
public class InternalUnifiedPushMessage extends UnifiedPushMessage {

    private static final long serialVersionUID = -4220526899054802147L;

    /**
     * Constant for the "push payload id" key
     */
    public static final String PUSH_MESSAGE_ID = "aerogear-push-id";

    private String ipAddress;
    private String clientIdentifier;

    /**
     * The IP address from the agent that did issue the push message request.
     *
     * @return the IP address
     */
    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    /**
     * The Client Identifier showing who triggered the Push Notification.
     *
     * @return client identifier string
     */
    public String getClientIdentifier() {
        return clientIdentifier;
    }

    public void setClientIdentifier(String clientIdentifier) {
        this.clientIdentifier = clientIdentifier;
    }
}
