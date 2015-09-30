package org.jboss.aerogear.unifiedpush.service;

import java.util.UUID;

public class TestUtils {
	public static String generateFakedDeviceTokenString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(UUID.randomUUID().toString());
        sb.append(UUID.randomUUID().toString());
        sb.append(UUID.randomUUID().toString());
        return sb.toString();
    }
}
