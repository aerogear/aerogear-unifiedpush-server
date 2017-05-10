package org.jboss.aerogear.unifiedpush.rest.documents;

import java.util.ArrayList;
import java.util.List;

import org.jboss.aerogear.unifiedpush.api.document.IDocumentList;
import org.jboss.aerogear.unifiedpush.cassandra.dao.model.parser.JsonDocumentContent;

import com.fasterxml.jackson.annotation.JsonProperty;

public final class DocumentList implements IDocumentList<JsonDocumentContent, String> {
	private final List<JsonDocumentContent> docs;
	private final List<String> ignoredIds;

	public DocumentList() {
		super();
		this.ignoredIds = new ArrayList<String>();
		this.docs = new ArrayList<JsonDocumentContent>();
	}

	@Override
	@JsonProperty
	public List<JsonDocumentContent> getDocuments() {
		return docs;
	}

	@Override
	@JsonProperty(required = false)
	public List<String> getIgnoredIds() {
		return ignoredIds;
	}

	public void ignore(String uuid) {
		ignoredIds.add(uuid);
	}
}
