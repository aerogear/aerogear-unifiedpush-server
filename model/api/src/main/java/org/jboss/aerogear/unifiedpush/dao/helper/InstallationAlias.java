package org.jboss.aerogear.unifiedpush.dao.helper;

public class InstallationAlias {
	private String id;
	private String alias;

	public InstallationAlias() {

	}

	public InstallationAlias(String id, String alias) {
		super();
		this.id = id;
		this.alias = alias;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}
}
