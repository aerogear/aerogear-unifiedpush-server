package org.jboss.aerogear.unifiedpush.dao;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import org.jboss.aerogear.unifiedpush.api.Alias;
import org.jboss.aerogear.unifiedpush.api.document.IDocument;
import org.jboss.aerogear.unifiedpush.api.document.QueryOptions;

public interface DocumentDao<T, ID> {

	IDocument<ID> create(IDocument<ID> document);

	void delete(UUID pushApplicationId);

	void delete(T doc);

	Stream<? extends IDocument<ID>> find(ID key, QueryOptions options);

	IDocument<ID> findOne(ID key);

	IDocument<ID> findOne(ID key, String documentId);

	List<IDocument<ID>> findLatestForAliases(ID key, List<Alias> aliases, String documentId);
}
