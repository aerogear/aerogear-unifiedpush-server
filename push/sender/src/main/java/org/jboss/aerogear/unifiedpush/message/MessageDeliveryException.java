package org.jboss.aerogear.unifiedpush.message;

public class MessageDeliveryException extends RuntimeException {

    private static final long serialVersionUID = 5679901095720892005L;

    public MessageDeliveryException() {
        super();
    }

    public MessageDeliveryException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public MessageDeliveryException(String message, Throwable cause) {
        super(message, cause);
    }

    public MessageDeliveryException(String message) {
        super(message);
    }

    public MessageDeliveryException(Throwable cause) {
        super(cause);
    }
}
