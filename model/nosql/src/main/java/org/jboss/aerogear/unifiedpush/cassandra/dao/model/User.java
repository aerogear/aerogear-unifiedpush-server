package org.jboss.aerogear.unifiedpush.cassandra.dao.model;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoField;
import java.util.UUID;

import javax.validation.constraints.NotNull;

import org.jboss.aerogear.unifiedpush.api.Alias;
import org.jboss.aerogear.unifiedpush.utils.UUIDToDate;
import org.springframework.data.annotation.Transient;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Table(value = "users")
public class User {

	@NotNull
	@PrimaryKey
	@JsonIgnore
	private UserKey key;

	@Column(value = "type")
	private Byte type;

	@Column(value = "month")
	private Byte month;

	@Column(value = "day")
	private Integer day;

	public User() {
		this(new UserKey(), (byte) AliasType.OTHER.ordinal());
	}

	private User(UserKey key, byte type) {
		super();
		this.key = key;

		// Time zone is not important here, month/day are used only as MV
		// partition key.
		if (key != null && key.getId() != null) {
			LocalDateTime date = LocalDateTime.ofInstant(Instant.ofEpochMilli(UUIDToDate.getTimeFromUUID(key.getId())), ZoneOffset.UTC);

			this.month = (byte) date.get(ChronoField.MONTH_OF_YEAR);
			this.day = date.get(ChronoField.DAY_OF_MONTH);
		}

		this.type = type;
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

	public Byte getType() {
		return type;
	}

	public void setType(Byte type) {
		this.type = type;
	}

	public static User copy(Alias alias, String aliasAttribute, int type) {
		return new User(new UserKey(alias.getPushApplicationId(), alias.getId(), aliasAttribute), (byte) type);
	}

	public enum AliasType {
		EMAIL, EMAIL_LOWER, OTHER;
	}
}
