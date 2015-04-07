package org.jboss.aerogear.unifiedpush.message;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

import org.jboss.aerogear.unifiedpush.api.PushMessageInformation;
import org.jboss.aerogear.unifiedpush.api.Variant;

public class MessageWithVariants implements Serializable {

    private static final long serialVersionUID = -7955411139315335655L;

    private PushMessageInformation pushMessageInformation;
    private UnifiedPushMessage unifiedPushMessage;
    private ArrayList<Variant> variants;


    public MessageWithVariants(PushMessageInformation pushMessageInformation, UnifiedPushMessage unifiedPushMessage, Collection<Variant> variants) {
        this.pushMessageInformation = pushMessageInformation;
        this.unifiedPushMessage = unifiedPushMessage;
        this.variants = new ArrayList<Variant>(variants);
    }

    public PushMessageInformation getPushMessageInformation() {
        return pushMessageInformation;
    }

    public UnifiedPushMessage getUnifiedPushMessage() {
        return unifiedPushMessage;
    }

    public ArrayList<Variant> getVariants() {
        return variants;
    }

}
