package org.jboss.aerogear.unifiedpush.service.impl;

import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UserTenantInfo {

	private final UUID userGuid;
	private final UUID pushId;
	private final String client;

	public UserTenantInfo(UUID userGuid, UUID pushId, String client) {
		this.userGuid = userGuid;
		this.pushId = pushId;
		this.client = client;
	}

	@JsonProperty("uid")
	public UUID getUserGuid() {
		return userGuid;
	}

	@JsonProperty("pid")
	public UUID getPushId() {
		return pushId;
	}

	@JsonProperty("cid")
	public String getClient() {
		return client;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		UserTenantInfo that = (UserTenantInfo) o;
		return getUserGuid().equals(that.getUserGuid()) &&
				getPushId().equals(that.getPushId()) &&
				getClient().equals(that.getClient());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getUserGuid(), getPushId(), getClient());
	}

	@Override
	public String toString() {
		return "UserTenantInfo{" +
				"userGuid=" + userGuid +
				", pushId=" + pushId +
				", client='" + client + '\'' +
				'}';
	}
}
