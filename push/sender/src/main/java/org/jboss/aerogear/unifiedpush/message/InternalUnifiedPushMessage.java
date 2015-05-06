package org.jboss.aerogear.unifiedpush.message;

/**
 * This is {@link UnifiedPushMessage} with some additional information, which we will use for internal purposes,
 * like analytics tracking.
 *
 * For more information see
 * <a href="https://issues.jboss.org/browse/AGPUSH-1381">https://issues.jboss.org/browse/AGPUSH-1381</a> and
 * <a href="https://github.com/aerogear/aerogear-unifiedpush-server/pull/526#discussion-diff-28432730">
 *     https://github.com/aerogear/aerogear-unifiedpush-server/pull/526#discussion-diff-28432730</a>
 */
public class InternalUnifiedPushMessage extends UnifiedPushMessage {

    private String ipAddress;
    private String clientIdentifier;

    /**
     * The IP address from the agent that did issue the push message request.
     */
    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    /**
     * The Client Identifier showing who triggered the Push Notification.
     */
    public String getClientIdentifier() {
        return clientIdentifier;
    }

    public void setClientIdentifier(String clientIdentifier) {
        this.clientIdentifier = clientIdentifier;
    }
}
