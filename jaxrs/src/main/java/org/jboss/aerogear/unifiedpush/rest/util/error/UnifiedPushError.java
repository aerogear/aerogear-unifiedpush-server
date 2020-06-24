package org.jboss.aerogear.unifiedpush.rest.util.error;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class UnifiedPushError {
    private final String message;
    private final Map<String, String> details = new HashMap<>();
    private Throwable rootException;

    UnifiedPushError(String message) {
        this.message = message;
    }

    void addDetail(final String key, final String value) {
        this.details.put(key, value);
    }

    void setRootException(Throwable exc) {
        this.rootException = exc;
    }

    public String getMessage() {
        return message;
    }
    public Map<String, String> getDetails() {
        return Collections.unmodifiableMap(this.details);
    }
    public Throwable getRootException() {
        return this.rootException;
    }
}
