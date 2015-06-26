package org.jboss.aerogear.unifiedpush.message.cache;

import java.util.concurrent.atomic.AtomicReference;

public class DisposableReference<T> {

    private AtomicReference<T> reference;
    private ServiceDestroyer<T> destroyer;

    public DisposableReference(T instance, ServiceDestroyer<T> destroyer) {
        this.reference = new AtomicReference<T>(instance);
        this.destroyer = destroyer;
    }

    public T get() {
        return reference.getAndSet(null);
    }

    public void dispose() {
        T instance = get();
        if (instance != null) {
            destroyer.destroy(instance);
        }
    }
}
