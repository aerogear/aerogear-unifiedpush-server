package org.jboss.aerogear.unifiedpush.cassandra.dao.model;

import javax.validation.constraints.NotNull;

import org.springframework.data.cassandra.mapping.PrimaryKey;
import org.springframework.data.cassandra.mapping.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Table(value = "otpcodes")
public class OtpCode {

	@NotNull
	@PrimaryKey
	@JsonIgnore
	private OtpCodeKey key;

	public OtpCode() {

	}

	public OtpCode(OtpCodeKey key) {
		super();
		this.key = key;
	}

	public OtpCodeKey getKey() {
		return key;
	}

	public void setKey(OtpCodeKey key) {
		this.key = key;
	}
}
