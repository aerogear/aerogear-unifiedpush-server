package org.jboss.aerogear.unifiedpush.kafka.serdes;

import net.wessendorf.kafka.serialization.GenericDeserializer;
import net.wessendorf.kafka.serialization.GenericSerializer;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;
import org.jboss.aerogear.unifiedpush.message.InternalUnifiedPushMessage;

import java.util.Map;

public class InternalUnifiedPushMessageSerde implements Serde<InternalUnifiedPushMessage> {
    final private Serializer<InternalUnifiedPushMessage> serializer = new GenericSerializer<>(InternalUnifiedPushMessage.class);
    final private Deserializer<InternalUnifiedPushMessage> deserializer = new GenericDeserializer<>(InternalUnifiedPushMessage.class);

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
    public Serializer<InternalUnifiedPushMessage> serializer() {
        return serializer;
    }

    @Override
    public Deserializer<InternalUnifiedPushMessage> deserializer() {
        return deserializer;
    }
}