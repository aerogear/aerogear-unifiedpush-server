package org.jboss.aerogear.unifiedpush.api;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Base64;

/**
 * This is a deserialized webpush token.
 */
public class WebPushRegistration {
    private String endpoint ="";
    private PushKey keys = new PushKey();

    @JsonIgnore
    public byte[] getAuthAsBytes() {
        return Base64.getDecoder().decode(keys.getAuth());
    }

    @JsonIgnore
    public byte[] getKeyAsBytes() {
        return Base64.getDecoder().decode(keys.getP256dh());
    }

    /**
     * @param endpoint the endpoint to set
     */
    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    /**
     * @param keys the keys to set
     */
    public void setKeys(PushKey keys) {
        this.keys = keys;
    }

    /**
     * @return the endpoint
     */
    public String getEndpoint() {
        return endpoint;
    }
    /**
     * @return the keys
     */
    public PushKey getKeys() {
        return keys;
    }

    public static class PushKey {
        private String p256dh = "";
        private String auth = "";

        /**
         * @return the auth
         */
        public String getAuth() {
            return auth;
        }

        /**
         * @return the p256dh
         */
        public String getP256dh() {
            return p256dh;
        }

        /**
         * @param auth the auth to set
         */
        public void setAuth(String auth) {
            this.auth = auth;
        }

        /**
         * @param p256dh the p256dh to set
         */
        public void setP256dh(String p256dh) {
            this.p256dh = p256dh;
        }

    }

}
