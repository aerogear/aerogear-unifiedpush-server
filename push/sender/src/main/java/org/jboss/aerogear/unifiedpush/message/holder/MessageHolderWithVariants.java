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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

import org.jboss.aerogear.unifiedpush.api.PushMessageInformation;
import org.jboss.aerogear.unifiedpush.api.Variant;
import org.jboss.aerogear.unifiedpush.api.VariantType;
import org.jboss.aerogear.unifiedpush.message.UnifiedPushMessage;

/**
 * Holds push message details with what type and list of variants should be the notification sent to.
 *
 * Holder is used as a payload in messaging subsystem.
 */
public class MessageHolderWithVariants extends AbstractMessageHolder implements Serializable {

    private static final long serialVersionUID = -7955411139315335655L;

    private VariantType variantType;
    private Collection<Variant> variants;
    private String lastTokenFromPreviousBatch;

    /**
     * Constructs holder that denotes first request for processing.
     *
     * Does not specify {@link #lastTokenFromPreviousBatch} as first request will start from first device token in database.
     */
    public MessageHolderWithVariants(PushMessageInformation pushMessageInformation, UnifiedPushMessage unifiedPushMessage, VariantType variantType, Collection<Variant> variants) {
        this(pushMessageInformation, unifiedPushMessage, variantType, variants, null);
    }

    /**
     * Constructs holder that denotes subsequent request for processing given push message, continuing from {@link #lastTokenFromPreviousBatch} where the previous request ended.
     */
    public MessageHolderWithVariants(PushMessageInformation pushMessageInformation, UnifiedPushMessage unifiedPushMessage, VariantType variantType, Collection<Variant> variants, String lastTokenFromPreviousBatch) {
        super(pushMessageInformation, unifiedPushMessage);
        this.variantType = variantType;
        this.variants = new ArrayList<Variant>(variants);
        this.lastTokenFromPreviousBatch = lastTokenFromPreviousBatch;
    }

    public VariantType getVariantType() {
        return variantType;
    }

    public Collection<Variant> getVariants() {
        return variants;
    }

    public String getLastTokenFromPreviousBatch() {
        return lastTokenFromPreviousBatch;
    }
}
