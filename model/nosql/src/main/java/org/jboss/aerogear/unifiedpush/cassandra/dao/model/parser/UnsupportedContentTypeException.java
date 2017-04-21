package org.jboss.aerogear.unifiedpush.cassandra.dao.model.parser;

public class UnsupportedContentTypeException extends RuntimeException {
	private static final long serialVersionUID = -7644029925614901121L;

	public UnsupportedContentTypeException(String msg) {
		super(msg);
	}
}
