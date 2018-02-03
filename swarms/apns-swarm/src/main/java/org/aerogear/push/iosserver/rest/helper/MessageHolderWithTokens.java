package org.aerogear.push.iosserver.rest.helper;

import org.jboss.aerogear.unifiedpush.api.FlatPushMessageInformation;
import org.jboss.aerogear.unifiedpush.api.Variant;
import org.jboss.aerogear.unifiedpush.api.iOSVariant;
import org.jboss.aerogear.unifiedpush.message.UnifiedPushMessage;

import java.io.Serializable;
import java.util.Collection;

public class MessageHolderWithTokens  {

    private static final long serialVersionUID = -7955411139315335655L;

    private int serialId;
    private iOSVariant variant;
    private Collection<String> deviceTokens;
    private FlatPushMessageInformation pushMessageInformation;
    private InternalUnifiedPushMessage unifiedPushMessage;


    public MessageHolderWithTokens() {

    }

    public MessageHolderWithTokens(FlatPushMessageInformation pushMessageInformation, UnifiedPushMessage unifiedPushMessage, Variant variant, Collection<String> deviceTokens, int serialId) {
        this.pushMessageInformation = pushMessageInformation;
        this.unifiedPushMessage = (InternalUnifiedPushMessage) unifiedPushMessage;
        if (!(deviceTokens instanceof Serializable)) {
            throw new IllegalArgumentException("deviceTokens must be a serializable collection");
        }
        this.variant = (iOSVariant) variant;
        this.deviceTokens = deviceTokens;
        this.serialId = serialId;
    }

    public Variant getVariant() {
        return variant;
    }

    public Collection<String> getDeviceTokens() {
        return deviceTokens;
    }

    public int getSerialId() {
        return serialId;
    }

    public FlatPushMessageInformation getPushMessageInformation() {
        return pushMessageInformation;
    }

    public InternalUnifiedPushMessage getUnifiedPushMessage() {
        return unifiedPushMessage;
    }


}


