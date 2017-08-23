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
package org.jboss.aerogear.unifiedpush.kafka.serdes;

import net.wessendorf.kafka.serialization.GenericDeserializer;
import net.wessendorf.kafka.serialization.GenericSerializer;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;
import org.jboss.aerogear.unifiedpush.api.PushApplication;

import java.util.Map;

/**
 * Serde to serialize and deserialize {@link PushApplication} objects
 * used in {@link org.jboss.aerogear.unifiedpush.kafka.streams.NotificationRouterStreamsHook}
 */
public class PushApplicationSerde implements Serde<PushApplication> {
    final private Serializer<PushApplication> serializer = new GenericSerializer(PushApplication.class);
    final private Deserializer<PushApplication> deserializer = new GenericDeserializer(PushApplication.class);

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        serializer.configure(configs, isKey);
        deserializer.configure(configs, isKey);
    }

    @Override
    public void close() {
        serializer.close();
        deserializer.close();
    }

    @Override
    public Serializer<PushApplication> serializer() {
        return serializer;
    }

    @Override
    public Deserializer<PushApplication> deserializer() {
        return deserializer;
    }
}