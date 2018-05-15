/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.unifiedpush.event;

import org.jboss.aerogear.unifiedpush.api.iOSVariant;

import java.util.Objects;

/**
 * Fired when the iOS variant is updated to contain a new .p12 file,
 * therefore the update event is used to trigger a removal from the internal connection cache.
 */
public class iOSVariantUpdateEvent {

    private iOSVariant iOSVariant;

    public iOSVariantUpdateEvent(iOSVariant iOSVariant) {
        this.iOSVariant = iOSVariant;
    }

    public iOSVariant getiOSVariant() {
        return iOSVariant;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof iOSVariantUpdateEvent)) return false;
        iOSVariantUpdateEvent that = (iOSVariantUpdateEvent) o;
        return Objects.equals(iOSVariant, that.iOSVariant);
    }

    @Override
    public int hashCode() {
        return Objects.hash(iOSVariant);
    }
}
