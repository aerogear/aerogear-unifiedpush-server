package org.jboss.aerogear.unifiedpush.api;

import org.jacoco.core.internal.data.CRC64;

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
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
		return this.name;
	}

    public void setName(String name) {
		this.name = name;
		setId(CRC64.checksum(name));
	}

	public String getPushApplicationID() {
		return pushApplicationID;
	}

    public void setPushApplicationID(String pushApplicationID) {
		this.pushApplicationID = pushApplicationID;
	}
}
