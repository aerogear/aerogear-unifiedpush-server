package org.jboss.aerogear.unifiedpush.api;

import java.io.Serializable;

public class Document implements Serializable {
	
	private static final long serialVersionUID = -6559883482588887839L;
	
	private String content;
	private String qualifier;
	
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getQualifier() {
		return qualifier;
	}
	public void setQualifier(String qualifier) {
		this.qualifier = qualifier;
	}
}
