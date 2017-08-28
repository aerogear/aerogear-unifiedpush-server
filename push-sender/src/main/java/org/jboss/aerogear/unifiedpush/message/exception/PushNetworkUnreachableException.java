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
package org.jboss.aerogear.unifiedpush.message.exception;

import org.jboss.aerogear.unifiedpush.message.sender.PushNotificationSender;

/**
 * Thrown when {@link PushNotificationSender} failed to establish connection to the Push Network.
 *
 * In such a case, the message will be considered not dispatched and messaging subsystem will try to deliver message later.
 *
 * Note: this exception should NOT be thrown when the variant setup is incorrect and Push Network refused connection (e.g.
 * because of failed authentication)
 */
public class PushNetworkUnreachableException extends DispatchInitiationException {

    private static final long serialVersionUID = -3279031099595849939L;

    public PushNetworkUnreachableException(String message, Throwable cause) {
        super(message, cause);
    }

    public PushNetworkUnreachableException(String message) {
        super(message);
    }

    public PushNetworkUnreachableException(Throwable cause) {
        super(cause);
    }
}
