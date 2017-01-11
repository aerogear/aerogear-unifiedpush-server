package org.jboss.aerogear.unifiedpush.cassandra.dao.model;

import javax.validation.constraints.NotNull;

import org.jboss.aerogear.unifiedpush.api.IDocument;
import org.jboss.aerogear.unifiedpush.cassandra.dao.impl.DocumentKey;
import org.springframework.data.cassandra.mapping.Column;
import org.springframework.data.cassandra.mapping.PrimaryKey;
import org.springframework.data.cassandra.mapping.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Table(value = "documents")
public class DocumentContent implements IDocument<DocumentKey> {

	@NotNull
	@PrimaryKey
	@JsonIgnore
	private DocumentKey key;

	@Column(value = "document_id")
	private String documentId;

	@Column
	private String content;

	@Column(value = "content_type")
	private String contentType;

	public DocumentContent() {
		super();
	}

	public DocumentContent(DocumentKey key, String content) {
		this(key, content, null);

	}

	public DocumentContent(DocumentKey key, String content, String documentId) {
		super();
		this.key = key;
		this.content = content;
		this.contentType = "application/json";
		this.documentId = documentId;
	}

	public DocumentKey getKey() {
		return key;
	}

	public void setKey(DocumentKey key) {
		this.key = key;
	}

	public String getDocumentId() {
		return documentId;
	}

	public void setDocumentId(String documentId) {
		this.documentId = documentId;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

}
