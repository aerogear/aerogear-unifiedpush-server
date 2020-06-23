package org.jboss.aerogear.unifiedpush.rest.util.error;

public class UnifiedPushError {
    private final String message;

    public UnifiedPushError(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
