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

import com.datastax.driver.core.utils.UUIDs;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Table(value = "users")
public class User {

	@NotNull
	@PrimaryKey
	@JsonIgnore
	private UserKey key;

	@Column
	private Byte month;

	@Column
	private Integer day;

	public User() {
		this(new UserKey());
	}

	public User(UserKey key) {
		super();
		this.key = key;

		// Time zone is not important here, month/day are used only as MV
		// partition key.
		if (key != null && key.getId() != null) {
			this.month = (byte) LocalDateTime.ofEpochSecond(UUIDToDate.getTimeFromUUID(key.getId()), 0, ZoneOffset.UTC)
					.get(ChronoField.MONTH_OF_YEAR);
			this.day = LocalDateTime.ofEpochSecond(UUIDToDate.getTimeFromUUID(key.getId()), 0, ZoneOffset.UTC)
					.get(ChronoField.DAY_OF_MONTH);
		}
	}

	public User(UUID pushApplicationId, String alias) {
		this(pushApplicationId, UUIDs.timeBased(), alias);
	}

	public User(UUID pushApplicationId, UUID id, String alias) {
		this(new UserKey(pushApplicationId, id, alias));
	}

	public UserKey getKey() {
		return key;
	}

	public void setKey(UserKey key) {
		this.key = key;
	}

	@Transient
	public UUID getId() {
		return getKey().getId();
	}

	public void setId(UUID id) {
		this.getKey().setId(id);
	}

	public String getAlias() {
		return this.getKey().getAlias();
	}

	public void setAlias(String alias) {
		this.getKey().setAlias(alias);
	}

	public Byte getMonth() {
		return month;
	}

	public Integer getDay() {
		return day;
	}

	public static User copy(Alias alias, String aliasAttribute) {
		return new User(new UserKey(alias.getPushApplicationId(), alias.getId(), aliasAttribute));
	}
}
