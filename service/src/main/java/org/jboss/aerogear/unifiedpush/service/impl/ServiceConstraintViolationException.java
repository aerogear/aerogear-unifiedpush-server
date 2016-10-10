package org.jboss.aerogear.unifiedpush.service.impl;

public class ServiceConstraintViolationException extends RuntimeException {
	private static final long serialVersionUID = -1278939900905611731L;

	private final String entityId;

	public ServiceConstraintViolationException(Throwable e, String entityId) {
		super(e);
		this.entityId = entityId;
	}

	public String getEntityId() {
		return entityId;
	}
}
