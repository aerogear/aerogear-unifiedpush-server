package org.jboss.aerogear.unifiedpush.message;

import java.io.Serializable;
import java.util.Collection;

import org.jboss.aerogear.unifiedpush.api.PushMessageInformation;
import org.jboss.aerogear.unifiedpush.api.Variant;

public class MessageWithTokens implements Serializable {

    private static final long serialVersionUID = -7955411139315335655L;

    private PushMessageInformation pushMessageInformation;
    private UnifiedPushMessage unifiedPushMessage;
    private Variant variant;
    private Collection<String> deviceTokens;

    public MessageWithTokens(PushMessageInformation pushMessageInformation, UnifiedPushMessage unifiedPushMessage, Variant variant, Collection<String> deviceTokens) {
        if (!(deviceTokens instanceof Serializable)) {
            throw new IllegalArgumentException("deviceTokens must be a serializable collection");
        }
        this.pushMessageInformation = pushMessageInformation;
        this.unifiedPushMessage = unifiedPushMessage;
        this.variant = variant;
        this.deviceTokens = deviceTokens;
    }

    public PushMessageInformation getPushMessageInformation() {
        return pushMessageInformation;
    }

    public UnifiedPushMessage getUnifiedPushMessage() {
        return unifiedPushMessage;
    }

    public Variant getVariant() {
        return variant;
    }

    public Collection<String> getDeviceTokens() {
        return deviceTokens;
    }
}
