/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.unifiedpush.message.serviceLease;

import java.util.concurrent.atomic.AtomicReference;

/**
 * A reference that is set and can be disposed later.
 *
 * A reference becomes null when retrieved or disposed.
 */
public class DisposableReference<T> {

    private AtomicReference<T> reference;
    private ServiceDestroyer<T> destroyer;

    /**
     * Create reference to instance together with a destroyer that will be applied to instance if it is being disposed.
     */
    public DisposableReference(T instance, ServiceDestroyer<T> destroyer) {
        this.reference = new AtomicReference<T>(instance);
        this.destroyer = destroyer;
    }

    /**
     * Retrieve the referenced instance and set reference to null.
     *
     * @return referenced instance or null if it was already retrieved or it was disposed
     */
    public T get() {
        return reference.getAndSet(null);
    }

    /**
     * Dispose the referenced instance (if available) and set reference to null.
     */
    public void dispose() {
        T instance = get();
        if (instance != null) {
            destroyer.destroy(instance);
        }
    }
}
