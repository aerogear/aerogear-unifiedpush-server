package org.jboss.aerogear.unifiedpush.cassandra.dao.model;

import java.util.UUID;

import javax.validation.constraints.NotNull;

import org.jboss.aerogear.unifiedpush.api.Alias;
import org.springframework.data.annotation.Transient;
import org.springframework.data.cassandra.mapping.Column;
import org.springframework.data.cassandra.mapping.PrimaryKey;
import org.springframework.data.cassandra.mapping.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Table(value = "users")
public class User extends Alias {

	@NotNull
	@PrimaryKey
	@JsonIgnore
	private UserKey key;

	public User() {
		super();
		this.key = new UserKey();
	}

	public User(UUID pushApplicationId) {
		super();
		this.key = new UserKey(pushApplicationId);
	}

	public User(UUID pushApplicationId, UUID id) {
		super();
		this.key = new UserKey(pushApplicationId, id);
	}

	public User(UserKey key) {
		super();
		this.key = key;
	}

	public UserKey getKey() {
		return key;
	}

	public void setKey(UserKey key) {
		this.key = key;
	}

	@Override
	@Transient
	public UUID getId() {
		return getKey().getId();
	}

	@Override
	public void setId(UUID id) {
		super.setId(id);
		this.getKey().setId(id);
	}

	@Override
	@Transient
	public UUID getPushApplicationId() {
		return this.getKey().getPushApplicationId();
	}

	@Override
	public void setPushApplicationId(UUID pushApplicationId) {
		super.setPushApplicationId(pushApplicationId);
		this.getKey().setPushApplicationId(pushApplicationId);
	}

	@Override
	@Column
	public String getEmail() {
		return super.getEmail();
	}

	@Override
	public void setEmail(String email) {
		super.setEmail(email);
	}

	@Override
	@Column
	public String getMobile() {
		return super.getMobile();
	}

	@Override
	public void setMobile(String mobile) {
		super.setMobile(mobile);
	}

	public User clone(Alias alias) {
		this.key = new UserKey(alias.getPushApplicationId(), alias.getId());
		this.setMobile(alias.getMobile());
		this.setEmail(alias.getEmail());

		return this;
	}
}
