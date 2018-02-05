package org.aerogear.push.apns.kafka;

import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;

import java.util.logging.Logger;

public class AeroGearSerdes extends Serdes {

    private static final Logger logger = Logger.getLogger(AeroGearSerdes.class.getName());

    /*
     * A generic serde for objects of type T.
     */
    static public <T> Serde<T> Generic(Class<T> type) {
        return new GenericSerde(type);
    }

    static public <T> Serde<T> serdeFrom(Class<T> type) {

        // look up default Kafka SerDes
        // if the class type is not supported an exception is thrown
        try {
            return Serdes.serdeFrom(type);
        }
        // If an exception is thrown, use custom generic serdes
        catch (IllegalArgumentException e) {
            logger.severe("Class type is not supported. Using generic serdes");
            return (Serde<T>) Generic(type);
        }
    }

    static public final class GenericSerde<T> extends WrapperSerde<T> {
        public GenericSerde(Class<T> type) {
            super(new GenericSerializer<T>(type), new GenericDeserializer<T>(type));
        }

    }
}
