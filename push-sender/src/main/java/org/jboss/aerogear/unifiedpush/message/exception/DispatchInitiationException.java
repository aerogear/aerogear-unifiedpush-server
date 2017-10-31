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

import org.jboss.aerogear.unifiedpush.message.NotificationDispatcher;
import org.jboss.aerogear.unifiedpush.message.sender.PushNotificationSender;

/**
 * Base for exceptions thrown by {@link PushNotificationSender} or {@link NotificationDispatcher} when sender failed to start
 * dispatching messages. In such a case it is safe to try to sent messages later as it is guarantee no message was dispatched yet.
 */
public abstract class DispatchInitiationException extends RuntimeException {

    private static final String MESSAGE = "Failed to initiate dispatch, the message will be re-delivered";

    private static final long serialVersionUID = 8379855199857322818L;

    public DispatchInitiationException(String message, Throwable cause) {
        super(String.format("%s: %s", MESSAGE, message), cause);
    }

    public DispatchInitiationException(String message) {
        super(String.format("%s: %s", MESSAGE, message));
    }

    public DispatchInitiationException(Throwable cause) {
        super(MESSAGE, cause);
    }
}
