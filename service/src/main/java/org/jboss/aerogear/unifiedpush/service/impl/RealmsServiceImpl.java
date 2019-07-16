package org.jboss.aerogear.unifiedpush.service.impl;

import java.util.Map;

import javax.annotation.PostConstruct;

import org.jboss.aerogear.unifiedpush.service.RealmsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

@Service
public class RealmsServiceImpl implements RealmsService {

    @Autowired
    private CacheManager cacheManager;

    private Cache appNameToRealm;

    @PostConstruct
    public void init() {
        appNameToRealm =  cacheManager.getCache(REALMS_CACHE_NAME);
    }

    @Override
    public void clearCache() {
        appNameToRealm.clear();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, String> getAll() {
        return (Map<String, String>) appNameToRealm.getNativeCache();
    }

    @Override
    public String get(String applicationName) {
        return appNameToRealm.get(applicationName, String.class);
    }

    @Override
    public void insert(String applicationName, String realmName) {
        appNameToRealm.put(applicationName, realmName);
    }
}
