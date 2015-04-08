package org.jboss.aerogear.unifiedpush.message;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

import org.jboss.aerogear.unifiedpush.api.PushMessageInformation;
import org.jboss.aerogear.unifiedpush.api.Variant;
import org.jboss.aerogear.unifiedpush.api.VariantType;

public class MessageWithVariants implements Serializable {

    private static final long serialVersionUID = -7955411139315335655L;

    private PushMessageInformation pushMessageInformation;
    private UnifiedPushMessage unifiedPushMessage;
    private VariantType variantType;
    private ArrayList<Variant> variants;


    public MessageWithVariants(PushMessageInformation pushMessageInformation, UnifiedPushMessage unifiedPushMessage, VariantType variantType, Collection<Variant> variants) {
        this.pushMessageInformation = pushMessageInformation;
        this.unifiedPushMessage = unifiedPushMessage;
        this.variantType = variantType;
        this.variants = new ArrayList<Variant>(variants);
    }

    public PushMessageInformation getPushMessageInformation() {
        return pushMessageInformation;
    }

    public VariantType getVariantType() {
        return variantType;
    }

    public UnifiedPushMessage getUnifiedPushMessage() {
        return unifiedPushMessage;
    }

    public ArrayList<Variant> getVariants() {
        return variants;
    }

}
