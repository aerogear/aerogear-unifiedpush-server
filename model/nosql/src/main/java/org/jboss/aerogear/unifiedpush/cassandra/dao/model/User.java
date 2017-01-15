package org.jboss.aerogear.unifiedpush.cassandra.dao.model;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoField;
import java.util.UUID;

import javax.validation.constraints.NotNull;

import org.jacoco.core.internal.data.UUIDToDate;
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

	@Column
	private Byte month;

	@Column
	private Integer day;

	public User() {
		super();
		this.key = new UserKey();
	}

	public User(UserKey key) {
		super();
		this.key = key;

		// Time zone is not important here, month/day are used only as MV
		// partition key.
		this.month = (byte) LocalDateTime.ofEpochSecond(UUIDToDate.getTimeFromUUID(key.getId()), 0, ZoneOffset.UTC)
				.get(ChronoField.MONTH_OF_YEAR);
		this.day = LocalDateTime.ofEpochSecond(UUIDToDate.getTimeFromUUID(key.getId()), 0, ZoneOffset.UTC)
				.get(ChronoField.DAY_OF_MONTH);
	}

	public User(UUID pushApplicationId) {
		this(new UserKey(pushApplicationId));
	}

	public User(UUID pushApplicationId, UUID id) {
		this(new UserKey(pushApplicationId, id));
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

	public Byte getMonth() {
		return month;
	}

	public Integer getDay() {
		return day;
	}

	public static User copy(Alias alias) {
		User user = new User(new UserKey(alias.getPushApplicationId(), alias.getId()));
		user.setMobile(alias.getMobile());
		user.setEmail(alias.getEmail());
		return user;
	}
}
