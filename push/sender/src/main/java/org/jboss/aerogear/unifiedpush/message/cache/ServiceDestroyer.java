package org.jboss.aerogear.unifiedpush.message.cache;

public interface ServiceDestroyer<T> {
    void destroy(T instance);
}