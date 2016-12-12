package org.jacoco.core.internal.data;

import java.util.UUID;

public enum NullUUID  {

    NULL( "00000000-0000-0000-0000-000000000000" );

    // Members
    private UUID uuid;

    // Constructor
	NullUUID ( String uuidHexArg ) {
        this.uuid = java.util.UUID.fromString( uuidHexArg );
    }

    // Getters
    public UUID getUuid ( ) {
        return this.uuid;
    }

}