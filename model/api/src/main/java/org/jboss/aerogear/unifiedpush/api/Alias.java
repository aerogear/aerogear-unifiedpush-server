package org.jboss.aerogear.unifiedpush.api;

import java.util.UUID;

public class Alias {
	private UUID id;
	private UUID pushApplicationId;
	private String email;
	private String other;

	public Alias() {
	}

	private Alias(UUID id) {
		super();
		this.id = id;
	}

	public Alias(UUID pushApplicationId, UUID id) {
		this(id);
		this.pushApplicationId = pushApplicationId;
	}

	public Alias(UUID pushApplicationId, UUID id, String email) {
		this(pushApplicationId, id);
		this.email = email;
	}

	public Alias(UUID pushApplicationId, UUID id, String email, String other) {
		this(pushApplicationId, id);
		this.email = email;
		this.other = other;
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

	public String getOther() {
		return other;
	}

	public void setOther(String other) {
		this.other = other;
	}

	@Override
	public String toString() {
		return "Alias [id=" + id + ", pushApplicationId=" + pushApplicationId + ", email=" + email + ", other=" + other
				+ "]";
	}


}
