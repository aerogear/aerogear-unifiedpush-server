package org.jboss.aerogear.unifiedpush.message.holder;

import java.io.Serializable;

import org.jboss.aerogear.unifiedpush.api.PushMessageInformation;
import org.jboss.aerogear.unifiedpush.message.UnifiedPushMessage;

public class AbstractMessageHolder implements Serializable {

    private static final long serialVersionUID = 8204829162844896312L;

    private PushMessageInformation pushMessageInformation;
    private UnifiedPushMessage unifiedPushMessage;

    public AbstractMessageHolder(PushMessageInformation pushMessageInformation, UnifiedPushMessage unifiedPushMessage) {
        this.pushMessageInformation = pushMessageInformation;
        this.unifiedPushMessage = unifiedPushMessage;
    }

    public PushMessageInformation getPushMessageInformation() {
        return pushMessageInformation;
    }

    public UnifiedPushMessage getUnifiedPushMessage() {
        return unifiedPushMessage;
    }

}