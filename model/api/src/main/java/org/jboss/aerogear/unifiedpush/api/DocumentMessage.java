package org.jboss.aerogear.unifiedpush.api;

import java.io.Serializable;

public class DocumentMessage implements Serializable {
	
	public static final String NULL_PART = "NULL";

	private static final long serialVersionUID = 1345771671978529056L;
	
	private String content;
	private DocumentMetadata metadata;

	public DocumentMessage() {
	}

	public DocumentMessage(String content, DocumentMetadata metadata) {
		this.content = content;
		this.metadata = metadata;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public DocumentMetadata getMetadata() {
		return metadata;
	}

	public void setMetadata(DocumentMetadata metadata) {
		this.metadata = metadata;
	}

}
