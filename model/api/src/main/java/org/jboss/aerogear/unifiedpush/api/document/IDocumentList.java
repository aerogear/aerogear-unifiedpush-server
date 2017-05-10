package org.jboss.aerogear.unifiedpush.api.document;

import java.util.List;

public interface IDocumentList<T extends IDocument<?, ?>, I> {
	List<T> getDocuments();

	List<I> getIgnoredIds();
}
