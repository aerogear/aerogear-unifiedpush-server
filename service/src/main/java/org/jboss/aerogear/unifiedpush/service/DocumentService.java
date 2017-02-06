package org.jboss.aerogear.unifiedpush.service;

import java.util.List;

import org.jboss.aerogear.unifiedpush.api.PushApplication;
import org.jboss.aerogear.unifiedpush.api.document.DocumentMetadata;
import org.jboss.aerogear.unifiedpush.api.document.QueryOptions;
import org.jboss.aerogear.unifiedpush.cassandra.dao.model.DocumentContent;
import org.jboss.aerogear.unifiedpush.document.MessagePayload;

public interface DocumentService {

	DocumentContent save(DocumentMetadata metadata, String content);

	String getLatestFromAlias(PushApplication pushApplication, String alias, String database, String id);

	void save(PushApplication pushApplication, MessagePayload payload, boolean overwrite);

	List<String> getLatestFromAliases(PushApplication pushApp, String database, String id);

	void delete(String pushApplicationId);

	List<DocumentContent> find(DocumentMetadata metadata, QueryOptions options);
}
