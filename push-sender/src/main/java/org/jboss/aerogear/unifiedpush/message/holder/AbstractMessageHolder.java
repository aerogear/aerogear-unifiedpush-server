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
package org.jboss.aerogear.unifiedpush.message.holder;

import org.jboss.aerogear.unifiedpush.api.FlatPushMessageInformation;
import org.jboss.aerogear.unifiedpush.message.UnifiedPushMessage;

import java.io.Serializable;

/**
 * Abstract class that serves as a base for objects that holds push notification details and can be used as a payload in messaging subsystems.
 */
public abstract class AbstractMessageHolder implements Serializable {

    private static final long serialVersionUID = 8204829162844896312L;

    private FlatPushMessageInformation pushMessageInformation;
    private UnifiedPushMessage unifiedPushMessage;
    private int retryCount = 0;

    public AbstractMessageHolder(FlatPushMessageInformation pushMessageInformation, UnifiedPushMessage unifiedPushMessage) {
        this.pushMessageInformation = pushMessageInformation;
        this.unifiedPushMessage = unifiedPushMessage;
    }

    public FlatPushMessageInformation getPushMessageInformation() {
        return pushMessageInformation;
    }

    public UnifiedPushMessage getUnifiedPushMessage() {
        return unifiedPushMessage;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void incrRetryCount() {
        retryCount++;
    }

}