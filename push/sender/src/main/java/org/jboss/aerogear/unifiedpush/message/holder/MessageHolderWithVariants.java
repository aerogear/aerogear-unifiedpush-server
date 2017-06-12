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
import org.jboss.aerogear.unifiedpush.api.Variant;
import org.jboss.aerogear.unifiedpush.api.VariantType;
import org.jboss.aerogear.unifiedpush.message.UnifiedPushMessage;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Holds push message details with what type and list of variants should be the notification sent to.
 *
 * Holder is used as a payload in messaging subsystem.
 */
public class MessageHolderWithVariants extends AbstractMessageHolder {

    private static final long serialVersionUID = -7955411139315335655L;

    /**
     * The serial ID of the first (initial) batch of tokens for given Push Message
     */
    public static final int INITIAL_SERIAL_ID = 0;

    private VariantType variantType;
    private Collection<Variant> variants;
    private int lastSerialId;
    private String lastTokenFromPreviousBatch;

    /**
     * Constructs holder that denotes first request for processing.
     *
     * Does not specify {@link #lastTokenFromPreviousBatch} as first request will start from first device token in database.
     *
     * @param pushMessageInformation the push message info object
     * @param unifiedPushMessage the push message
     * @param variantType variant type info
     * @param variants list of effected variants
     */
    public MessageHolderWithVariants(FlatPushMessageInformation pushMessageInformation, UnifiedPushMessage unifiedPushMessage, VariantType variantType, Collection<Variant> variants) {
        this(pushMessageInformation, unifiedPushMessage, variantType, variants, INITIAL_SERIAL_ID, null);
    }

    /**
     * Constructs holder that denotes subsequent request for processing given push message, continuing from {@link #lastTokenFromPreviousBatch} where the previous request ended.
     *
     * @param pushMessageInformation the push message info object
     * @param unifiedPushMessage the push message
     * @param variantType variant type info
     * @param variants list of effected variants
     * @param lastSerialId last id from previous batch
     * @param lastTokenFromPreviousBatch last token from previous stream
     */
    public MessageHolderWithVariants(FlatPushMessageInformation pushMessageInformation, UnifiedPushMessage unifiedPushMessage, VariantType variantType, Collection<Variant> variants, int lastSerialId, String lastTokenFromPreviousBatch) {
        super(pushMessageInformation, unifiedPushMessage);
        this.variantType = variantType;
        this.variants = new ArrayList<>(variants);
        this.lastSerialId = lastSerialId;
        this.lastTokenFromPreviousBatch = lastTokenFromPreviousBatch;
    }

    public VariantType getVariantType() {
        return variantType;
    }

    public Collection<Variant> getVariants() {
        return variants;
    }

    public int getLastSerialId() {
        return lastSerialId;
    }

    public String getLastTokenFromPreviousBatch() {
        return lastTokenFromPreviousBatch;
    }
}
