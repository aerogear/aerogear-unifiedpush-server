package org.aerogear.push.apns.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.aerogear.push.apns.helper.MessageHolderWithTokens;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;

import java.io.IOException;
import java.util.Map;

public class GenericDeserializer<T> implements Deserializer<T> {

    private final Class<T> type = (Class<T>) MessageHolderWithTokens.class;
    private ObjectMapper mapper = new ObjectMapper();

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
    }

    @Override
    public T deserialize(String topic, byte[] data) {
        if(data == null) {
            return null;
        }

        try {
            return mapper.readValue(data, type);
        } catch (IOException e) {
            throw new SerializationException("Unable to deserialize object", e);
        }
    }

    @Override
    public void close() {
    }
}
