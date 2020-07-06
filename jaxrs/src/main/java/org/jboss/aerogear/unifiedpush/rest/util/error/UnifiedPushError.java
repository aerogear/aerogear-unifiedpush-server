package org.jboss.aerogear.unifiedpush.rest.util.error;

import com.fasterxml.jackson.annotation.JsonInclude;

import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class UnifiedPushError {
    @NotNull
    private final String message;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final Map<String, String> details = new HashMap<>();

    @JsonInclude(JsonInclude.Include.NON_NULL)
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
