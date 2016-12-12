package org.jboss.aerogear.unifiedpush.api;

import java.util.UUID;

public class Alias {
	private UUID pushApplicationId;
	private UUID id;
	private String email;
	private String mobile;

	public Alias() {
	}

	private Alias(UUID pushApplicationId) {
		super();
		this.pushApplicationId = pushApplicationId;
	}

	public Alias(UUID pushApplicationId, UUID id) {
		this(pushApplicationId);
		this.id = id;
	}

	public Alias(UUID pushApplicationId, UUID id, String email) {
		this(pushApplicationId, id);
		this.email = email;
	}

	public UUID getId() {
        return this.id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

	public UUID getPushApplicationId() {
		return pushApplicationId;
	}

    public void setPushApplicationId(UUID pushApplicationId) {
		this.pushApplicationId = pushApplicationId;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}
}
