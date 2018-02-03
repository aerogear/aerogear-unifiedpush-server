package org.aerogear.push.iosserver.rest.helper;

import org.jboss.aerogear.unifiedpush.message.UnifiedPushMessage;

public class InternalUnifiedPushMessage extends UnifiedPushMessage {

    private static final long serialVersionUID = -4220526899054802147L;

    /**
     * Constant for the "push payload id" key
     */
    public static final String PUSH_MESSAGE_ID = "aerogear-push-id";

    private String ipAddress;
    private String clientIdentifier;

    /**
     * The IP address from the agent that did issue the push message request.
     *
     * @return the IP address
     */
    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    /**
     * The Client Identifier showing who triggered the Push Notification.
     *
     * @return client identifier string
     */
    public String getClientIdentifier() {
        return clientIdentifier;
    }

    public void setClientIdentifier(String clientIdentifier) {
        this.clientIdentifier = clientIdentifier;
    }
}

