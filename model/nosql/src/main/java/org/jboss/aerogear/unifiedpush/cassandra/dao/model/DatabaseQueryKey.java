package org.jboss.aerogear.unifiedpush.cassandra.dao.model;

import java.io.Serializable;
import java.util.UUID;

public class DatabaseQueryKey implements Serializable {
	private static final long serialVersionUID = 3613590444329269400L;
	
	private UUID pushApplicationId;
	private String database;

	public DatabaseQueryKey(Database db) {
		super();
		this.pushApplicationId = db.getKey().getPushApplicationId();
		this.database = db.getDatabase();
	}

	public DatabaseQueryKey(DocumentContent doc) {
		super();
		this.pushApplicationId = doc.getKey().getPushApplicationId();
		this.database = doc.getKey().getDatabase();
	}

	public DatabaseQueryKey(String pushApplicationId, String database) {
		super();
		this.pushApplicationId = UUID.fromString(pushApplicationId);
		this.database = database;
	}

	public UUID getPushApplicationId() {
		return pushApplicationId;
	}

	public void setPushApplicationId(UUID pushApplicationId) {
		this.pushApplicationId = pushApplicationId;
	}


	public String getDatabase() {
		return database;
	}


	public void setDatabase(String database) {
		this.database = database;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((database == null) ? 0 : database.hashCode());
		result = prime * result + ((pushApplicationId == null) ? 0 : pushApplicationId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DatabaseQueryKey other = (DatabaseQueryKey) obj;
		if (database == null) {
			if (other.database != null)
				return false;
		} else if (!database.equals(other.database))
			return false;
		if (pushApplicationId == null) {
			if (other.pushApplicationId != null)
				return false;
		} else if (!pushApplicationId.equals(other.pushApplicationId))
			return false;
		return true;
	}

}
