package org.jboss.aerogear.unifiedpush.cassandra.dao.model;

import java.util.UUID;

import javax.validation.constraints.NotNull;

import org.springframework.data.cassandra.mapping.Column;
import org.springframework.data.cassandra.mapping.PrimaryKey;
import org.springframework.data.cassandra.mapping.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Table(value = "databases")
public class Database {
	@NotNull
	@PrimaryKey
	@JsonIgnore
	private DatabaseKey key;

	@Column
	private String database;

	public Database() {
	}

	public Database(DatabaseQueryKey qkey) {
		super();
		this.key = new DatabaseKey(qkey.getPushApplicationId());
		this.database = qkey.getDatabase();
	}

	public Database(UUID pushApplicationId, String database) {
		super();
		this.key = new DatabaseKey(pushApplicationId);
		this.database = database;
	}

	public String getDatabase() {
		return database;
	}

	public void setDatabase(String database) {
		this.database = database;
	}

	public DatabaseKey getKey() {
		return key;
	}

	public void setKey(DatabaseKey key) {
		this.key = key;
	}
}
