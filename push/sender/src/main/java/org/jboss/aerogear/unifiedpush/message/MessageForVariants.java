package org.jboss.aerogear.unifiedpush.message;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

import org.jboss.aerogear.unifiedpush.api.Variant;

public class MessageForVariants implements Serializable {

    private static final long serialVersionUID = -7955411139315335655L;

    private UnifiedPushMessage unifiedPushMessage;
    private ArrayList<Variant> variants;

    public MessageForVariants(UnifiedPushMessage unifiedPushMessage, Collection<Variant> variants) {
        this.unifiedPushMessage = unifiedPushMessage;
        this.variants = new ArrayList<Variant>(variants);
    }

    public UnifiedPushMessage getUnifiedPushMessage() {
        return unifiedPushMessage;
    }

    public ArrayList<Variant> getVariants() {
        return variants;
    }

}
