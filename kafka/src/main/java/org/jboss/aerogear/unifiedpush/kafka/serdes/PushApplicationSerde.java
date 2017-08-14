package org.jboss.aerogear.unifiedpush.kafka.serdes;

import net.wessendorf.kafka.serialization.GenericDeserializer;
import net.wessendorf.kafka.serialization.GenericSerializer;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;
import org.jboss.aerogear.unifiedpush.api.PushApplication;

import java.util.Map;

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