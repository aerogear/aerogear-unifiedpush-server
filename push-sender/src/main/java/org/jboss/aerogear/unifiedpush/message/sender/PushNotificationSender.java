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
package org.jboss.aerogear.unifiedpush.message.sender;

import org.jboss.aerogear.unifiedpush.api.Variant;
import org.jboss.aerogear.unifiedpush.message.UnifiedPushMessage;

import java.util.Collection;

/**
 * Each implementation deals with the specific of the underlying push network, including transforming the content of the
 * {@link org.jboss.aerogear.unifiedpush.message.UnifiedPushMessage} to the proper message format of the actual push network and maintaining the connection to it.
 */
public interface PushNotificationSender {

    /**
     * Sends the {@link UnifiedPushMessage} to the given clients, identified by a collection of tokens, the underlying push network.
     *
     * @param variant contains details for the underlying push network, e.g. API Keys/Ids
     * @param clientIdentifiers platform specific collection of client identifiers
     * @param pushMessage payload to be send to the given clients
     * @param pushMessageInformationId the id of the FlatPushMessageInformation instance associated with this send.
     * @param senderCallback invoked after submitting the request to the underlying push network to indicate the status
     *                       of the request (<code>success</code> or <code>error</code>
     */
    void sendPushMessage(Variant variant, Collection<String> clientIdentifiers, UnifiedPushMessage pushMessage, String pushMessageInformationId, NotificationSenderCallback senderCallback);
}
