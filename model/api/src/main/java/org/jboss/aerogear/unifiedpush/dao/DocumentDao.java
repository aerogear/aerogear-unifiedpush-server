package org.jboss.aerogear.unifiedpush.dao;

import java.util.List;

import org.jboss.aerogear.unifiedpush.api.DocumentMessage;
import org.jboss.aerogear.unifiedpush.api.DocumentMetadata;
import org.jboss.aerogear.unifiedpush.api.PushApplication;

public interface DocumentDao {

	void create(DocumentMessage document, boolean overwrite);

	void delete(PushApplication app);

	DocumentMessage findLatestDocumentForAlias(DocumentMetadata message);

	List<DocumentMessage> findLatestDocumentsForApplication(DocumentMetadata message);

	List<DocumentMessage> findDocuments(DocumentMetadata message);

}
