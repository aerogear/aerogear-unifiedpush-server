package org.jboss.aerogear.unifiedpush.message.holder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

import org.jboss.aerogear.unifiedpush.api.PushMessageInformation;
import org.jboss.aerogear.unifiedpush.api.Variant;
import org.jboss.aerogear.unifiedpush.api.VariantType;
import org.jboss.aerogear.unifiedpush.message.UnifiedPushMessage;

public class MessageHolderWithVariants extends AbstractMessageHolder implements Serializable {

    private static final long serialVersionUID = -7955411139315335655L;

    private VariantType variantType;
    private ArrayList<Variant> variants;
    private String lastTokenFromPreviousBatch;

    public MessageHolderWithVariants(PushMessageInformation pushMessageInformation, UnifiedPushMessage unifiedPushMessage, VariantType variantType, Collection<Variant> variants) {
        this(pushMessageInformation, unifiedPushMessage, variantType, variants, null);
    }

    public MessageHolderWithVariants(PushMessageInformation pushMessageInformation, UnifiedPushMessage unifiedPushMessage, VariantType variantType, Collection<Variant> variants, String lastTokenFromPreviousBatch) {
        super(pushMessageInformation, unifiedPushMessage);
        this.variantType = variantType;
        this.variants = new ArrayList<Variant>(variants);
        this.lastTokenFromPreviousBatch = lastTokenFromPreviousBatch;
    }

    public VariantType getVariantType() {
        return variantType;
    }

    public ArrayList<Variant> getVariants() {
        return variants;
    }

    public String getLastTokenFromPreviousBatch() {
        return lastTokenFromPreviousBatch;
    }
}
