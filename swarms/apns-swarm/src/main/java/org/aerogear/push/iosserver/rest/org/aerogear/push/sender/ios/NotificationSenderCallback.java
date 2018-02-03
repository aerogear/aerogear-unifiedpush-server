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
package org.aerogear.push.iosserver.rest.org.aerogear.push.sender.ios;

/**
 * A simple Callback interface used when sending {@link org.jboss.aerogear.unifiedpush.message.UnifiedPushMessage} to
 * an actual push network.
 */
public interface NotificationSenderCallback {

    /**
     * Simple indicator which will be called on a successful to deliver to the push network. However, the invocation of
     * this callback does <b>NOT</b> mean the messages have been sent out to the mobile devices.
     */
    void onSuccess();

    /**
     * Simple indicator which will be called on any type of error that occurred while sending the payload to the
     * underlying push network.
     *
     * @param reason details about the error
     */
    void onError(String reason);

}
