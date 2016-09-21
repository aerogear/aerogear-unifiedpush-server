package org.jboss.aerogear.unifiedpush.service;

import java.util.List;

public class MergeResponse {
	private List<String> toEnable;
	private List<String> toDisable;

	public List<String> getToEnable() {
		return toEnable;
	}

	public void setToEnable(List<String> toEnable) {
		this.toEnable = toEnable;
	}

	public List<String> getToDisable() {
		return toDisable;
	}

	public void setToDisable(List<String> toDisable) {
		this.toDisable = toDisable;
	}
}
