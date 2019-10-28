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



import java.util.Objects;
import org.jboss.aerogear.unifiedpush.api.APNSVariant;

/**
 * Fired when the apns variant is updated to contain a new .p12 file,
 * therefore the update event is used to trigger a removal from the internal connection cache.
 */
public class APNSVariantUpdateEvent {

    private APNSVariant apnsVariant;

    public APNSVariantUpdateEvent(APNSVariant apnsVariant) {
        this.apnsVariant = apnsVariant;
    }

    public APNSVariant getApnsVariant() {
        return apnsVariant;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof APNSVariantUpdateEvent)) return false;
        APNSVariantUpdateEvent that = (APNSVariantUpdateEvent) o;
        return Objects.equals(apnsVariant, that.apnsVariant);
    }

    @Override
    public int hashCode() {
        return Objects.hash(apnsVariant);
    }
}
