package org.aerogear.connectivity.api;

/**
 * One installation of a MobileVariant, running on a device or user-agent.
 */
public interface MobileVariantInstance {
    
    /**
     * Identifies the device/user-agent within its PushNetwork.
     * 
     * <ul>
     * <li> APNs: <code>deviceToken</code>
     * <li> GCM: <code>registrationId</code>
     * <li> SimplePush: <code>channelId</code>
     * </ul>
     * 
     */
    void setDeviceToken(final String deviceToken);
    String getDeviceToken();

    /**
     * The device type of the device or the user agent.
     * 
     * <li> For SimplePush this will be <code>Web</code>
     * <li> For iOS that could be <code>iPod</code>, <code>iPad</code> or <code>iPhone</code>
     * <li> For Android that could be <code>Phone</code> or <code>Tablet</code>
     * 
     */
    void setDeviceType(final String deviceType);
    String getDeviceType();

    /**
     * The (optional) name of the underlying Operating System.
     */
    void setMobileOperatingSystem(final String mobileOperatingSystem);
    String getMobileOperatingSystem();

    /**
     * The (optional) version of the used Operating System.
     */
    void setOsVersion(final String osVersion);
    String getOsVersion();

    /**
     * Application specific alias to identify users with the system. 
     * E.g. email address or username
     */
    void setClientIdentifier(final String clientIdentifier);
    String getClientIdentifier();

    /**
     * Used for SimplePush, to "tag" the channel
     */
    void setCategory(final String category);
    String getCategory();
}
