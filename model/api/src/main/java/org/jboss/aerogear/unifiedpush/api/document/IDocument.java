package org.jboss.aerogear.unifiedpush.api.document;

import com.fasterxml.jackson.annotation.JsonIgnore;

public interface IDocument<ID, CT> {

	@JsonIgnore
	ID getKey();

	void setKey(ID key);

	CT getContent();

	void setContent(CT content);

	String getContentType();

	void setContentType(String contentType);

	String getDocumentId();

	void setDocumentId(String documentId);
}
