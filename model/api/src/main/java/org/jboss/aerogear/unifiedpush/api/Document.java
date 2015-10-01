package org.jboss.aerogear.unifiedpush.api;

import java.io.Serializable;

public class Document implements Serializable {
	
	private static final long serialVersionUID = -6559883482588887839L;

	private String type;
	private String content;
	
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
}
