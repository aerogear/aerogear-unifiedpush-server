package org.jboss.aerogear.unifiedpush.rest.documents;

import java.util.UUID;

import javax.ws.rs.core.MediaType;

import org.jboss.aerogear.unifiedpush.api.Installation;
import org.jboss.aerogear.unifiedpush.api.document.IDocument;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class DocumentInstallationWrapper implements IDocument<String, Installation> {

	private Installation content;

	public DocumentInstallationWrapper(Installation content) {
		super();
		this.content = content;
	}

	@Override
	@JsonIgnore
	public String getKey() {
		return null;
	}

	@Override
	public void setKey(String key) {

	}

	@Override
	public Installation getContent() {
		return content;
	}

	@Override
	public void setContent(Installation content) {
		this.content = content;
	}

	@Override
	public String getContentType() {
		return MediaType.APPLICATION_JSON;
	}

	@Override
	public void setContentType(String contentType) {

	}

	@Override
	public String getDocumentId() {
		return UUID.randomUUID().toString();
	}

	@Override
	public void setDocumentId(String documentId) {

	}

}
