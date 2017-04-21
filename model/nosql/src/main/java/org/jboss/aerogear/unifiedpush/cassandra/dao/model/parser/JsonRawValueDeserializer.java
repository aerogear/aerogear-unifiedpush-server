package org.jboss.aerogear.unifiedpush.cassandra.dao.model.parser;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;


public class JsonRawValueDeserializer extends JsonDeserializer<String> {

	@Override
	public String deserialize(JsonParser jp, DeserializationContext context) throws IOException {
		return jp.readValueAsTree().toString();
	}
}