package org.aerogear.push.apns.kafka;



import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Map;

public class GenericSerializer<T> implements Serializer<T> {

    private Class<T> type;
    private ObjectMapper mapper = new ObjectMapper();

    public GenericSerializer() {
    }

    public GenericSerializer(Class<T> type) {
        this.type = type;
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
    }

    @Override
    public byte[] serialize(String topic, T data) {
        if (data == null) {
            return null;
        }

        try {
            return mapper.writeValueAsBytes(data);
        } catch (JsonProcessingException e) {
            throw new SerializationException("Unable to serialize object", e);
        }
    }

    @Override
    public void close() {
    }
}