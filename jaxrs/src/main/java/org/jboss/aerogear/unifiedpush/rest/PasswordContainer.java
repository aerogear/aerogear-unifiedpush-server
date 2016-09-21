package org.jboss.aerogear.unifiedpush.rest;

import org.apache.commons.lang.StringUtils;

public class PasswordContainer{
	private String currentPassword;
	private String newPassword;
	
	public PasswordContainer() {
		super();
	}
	
	public PasswordContainer(String currentPassword, String newPassword) {
		this.currentPassword = currentPassword;
		this.newPassword = newPassword;
	}

	public String getCurrentPassword() {
		return currentPassword;
	}

	public void setCurrentPassword(String currentPassword) {
		this.currentPassword = currentPassword;
	}

	public String getNewPassword() {
		return newPassword;
	}

	public void setNewPassword(String currentPassword) {
		this.newPassword = currentPassword;
	}
	
	public boolean isDataValid(){
		return this != null && StringUtils.isNotBlank(this.currentPassword) && StringUtils.isNotBlank(this.newPassword); 
	}
}