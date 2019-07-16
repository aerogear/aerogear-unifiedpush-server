package org.jboss.aerogear.unifiedpush.service;

import java.util.Map;

public interface RealmsService {

    public static final String REALMS_CACHE_NAME = "application-to-realm";

    void clearCache();

    Map<String, String> getAll();

    String get(String applicationName);

    void insert(String applicationName, String realmName);
}
