package org.jboss.aerogear.unifiedpush.cassandra.dao;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import org.jboss.aerogear.unifiedpush.api.Alias;
import org.jboss.aerogear.unifiedpush.api.document.QueryOptions;
import org.jboss.aerogear.unifiedpush.cassandra.dao.model.DocumentContent;

public interface DocumentDao<T, ID> {

	DocumentContent create(DocumentContent document);
	
	DocumentContent create(DocumentContent document, int ttl);

	void delete(UUID pushApplicationId);

	void delete(UUID pushApplicaitonId, Alias alias);

	void delete(T doc);

	Stream<DocumentContent> find(ID key, QueryOptions options);

	Optional<DocumentContent> findOne(ID key);

	DocumentContent findOne(ID key, String documentId);

	List<DocumentContent> findLatestForAliases(ID key, List<Alias> aliases, String documentId);
}
