package org.jboss.aerogear.unifiedpush.dao;

public class BatchException extends Exception {

    private static final long serialVersionUID = 1L;

    public BatchException() {
        super();
    }

    public BatchException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public BatchException(String message, Throwable cause) {
        super(message, cause);
    }

    public BatchException(String message) {
        super(message);
    }

    public BatchException(Throwable cause) {
        super(cause);
    }



}
