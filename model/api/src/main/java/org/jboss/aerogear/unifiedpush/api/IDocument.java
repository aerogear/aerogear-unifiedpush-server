package org.jboss.aerogear.unifiedpush.api;

public interface IDocument<ID> {

	ID getKey();

	void setKey(ID key);

	String getContent();

	void setContent(String content);

	String getDocumentId();

	void setDocumentId(String documentId);
}
