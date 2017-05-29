package org.jboss.aerogear.unifiedpush.cassandra.dao.model.parser;

import java.util.UUID;

import org.jboss.aerogear.unifiedpush.cassandra.dao.impl.DocumentKey;
import org.jboss.aerogear.unifiedpush.cassandra.dao.model.DocumentContent;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * JSON wrapper parser to DocumentContent. JSON is serialized by including
 * literal String content as is, without quoting of characters.
 * <p>
 * Clients (JAVA) might serialize using {@link IDocument} derived classes,
 * therefore @JsonIgnoreProperties is enabled.
 * <p>
 * Warning: the resulting JSON stream may be invalid depending on your input
 * value.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class JsonDocumentContent extends DocumentContent {
	private static final String CONTENT_TYPE = "application/json";

	public JsonDocumentContent() {
		super();
		super.setContentType(CONTENT_TYPE);
	}

	public JsonDocumentContent(DocumentKey key, String content, String documentId) {
		super(key, content, documentId);
	}

	public JsonDocumentContent(DocumentKey key, String content) {
		super(key, content);
	}

	@JsonRawValue
	@Override
	public String getContent() {
		return super.getContent();
	}

	@JsonDeserialize(using = JsonRawValueDeserializer.class)
	@Override
	public void setContent(String content) {
		super.setContent(content.toString());
	}

	@Override
	public void setContentType(String contentType) {
		if (!CONTENT_TYPE.equals(contentType)) {
			throw new UnsupportedContentTypeException(String.format("Only %s type is permitted.", CONTENT_TYPE));
		}
		super.setContentType(contentType);
	}

	@JsonProperty(required = false)
	public UUID getSnapshot() {
		return super.getKey().getSnapshot();
	}
}
