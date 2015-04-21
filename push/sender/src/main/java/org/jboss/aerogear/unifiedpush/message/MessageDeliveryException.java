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
package org.jboss.aerogear.unifiedpush.message;

/**
 * Thrown when messaging subsystem fails to queue the notification for processing, so that the push notification couldn't be delivered
 */
public class MessageDeliveryException extends RuntimeException {

    private static final long serialVersionUID = 5679901095720892005L;

    public MessageDeliveryException() {
        super();
    }

    public MessageDeliveryException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public MessageDeliveryException(String message, Throwable cause) {
        super(message, cause);
    }

    public MessageDeliveryException(String message) {
        super(message);
    }

    public MessageDeliveryException(Throwable cause) {
        super(cause);
    }
}
