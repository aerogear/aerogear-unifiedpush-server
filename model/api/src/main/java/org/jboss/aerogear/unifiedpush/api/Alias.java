package org.jboss.aerogear.unifiedpush.api;

public class Alias {
	private Long id;
	private String name;
	private String pushApplicationID;

	public Alias() {
	}

	public Alias(String name, String pushApplicationID) {
		super();
		this.name = name;
		this.pushApplicationID = pushApplicationID;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPushApplicationID() {
		return pushApplicationID;
	}

	public void setPushApplicationID(String pushApplicationID) {
		this.pushApplicationID = pushApplicationID;
	}
}
