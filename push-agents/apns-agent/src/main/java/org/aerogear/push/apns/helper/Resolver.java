package org.aerogear.push.apns.helper;

public final class Resolver {

    private Resolver() {

    }

    public static String resolve(final String variable) {

        String value = System.getProperty(variable);
        if (value == null) {
            // than we try ENV ...
            value = System.getenv(variable);
        }
        return value;
    }
}
