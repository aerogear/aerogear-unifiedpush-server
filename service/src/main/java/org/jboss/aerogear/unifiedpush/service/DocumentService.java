package org.jboss.aerogear.unifiedpush.service;

import java.util.List;
import java.util.stream.Stream;

import org.jboss.aerogear.unifiedpush.api.Alias;
import org.jboss.aerogear.unifiedpush.api.PushApplication;
import org.jboss.aerogear.unifiedpush.api.document.DocumentMetadata;
import org.jboss.aerogear.unifiedpush.api.document.QueryOptions;
import org.jboss.aerogear.unifiedpush.cassandra.dao.model.DocumentContent;

public interface DocumentService {

	DocumentContent save(DocumentContent doc);

	DocumentContent save(DocumentMetadata metadata, String content, String id);

	void delete(String pushApplicationId);

	DocumentContent findLatest(DocumentMetadata metadata, String id);

	Stream<DocumentContent> find(DocumentMetadata metadata, QueryOptions options);

	List<DocumentContent> findLatest(PushApplication pushApp, String database, String id, List<Alias> aliases);
}
