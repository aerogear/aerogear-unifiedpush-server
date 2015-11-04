package org.jboss.aerogear.unifiedpush.api;

import java.io.Serializable;

public class DocumentMessage implements Serializable {
	private static final long serialVersionUID = 1345771671978529056L;
	
	public static enum DocumentType {
		APPLICATION_DOCUMENT,
		INSTALLATION_DOCUMENT
	};

	private DocumentType type;
	
	private String content;
	private String source;
	private String destination;
	private String qualifier;
	
	public DocumentType getType() {
		return type;
	}
	public void setType(DocumentType type) {
		this.type = type;
	}
	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}
	public String getDestination() {
		return destination;
	}
	public void setDestination(String destination) {
		this.destination = destination;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getQualifier() {
		return qualifier;
	}
	public void setQualifier(String qualifier) {
		this.qualifier = qualifier;
	}
}
