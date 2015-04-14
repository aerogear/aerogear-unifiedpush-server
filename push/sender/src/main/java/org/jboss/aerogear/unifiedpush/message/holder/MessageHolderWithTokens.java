package org.jboss.aerogear.unifiedpush.message.holder;

import java.io.Serializable;
import java.util.Collection;

import org.jboss.aerogear.unifiedpush.api.PushMessageInformation;
import org.jboss.aerogear.unifiedpush.api.Variant;
import org.jboss.aerogear.unifiedpush.message.UnifiedPushMessage;

public class MessageHolderWithTokens extends AbstractMessageHolder implements Serializable {

    private static final long serialVersionUID = -7955411139315335655L;

    private Variant variant;
    private Collection<String> deviceTokens;

    public MessageHolderWithTokens(PushMessageInformation pushMessageInformation, UnifiedPushMessage unifiedPushMessage, Variant variant, Collection<String> deviceTokens) {
        super(pushMessageInformation, unifiedPushMessage);
        if (!(deviceTokens instanceof Serializable)) {
            throw new IllegalArgumentException("deviceTokens must be a serializable collection");
        }
        this.variant = variant;
        this.deviceTokens = deviceTokens;
    }

    public Variant getVariant() {
        return variant;
    }

    public Collection<String> getDeviceTokens() {
        return deviceTokens;
    }
}
