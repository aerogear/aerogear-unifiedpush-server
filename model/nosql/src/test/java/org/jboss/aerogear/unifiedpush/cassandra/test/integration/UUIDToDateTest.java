package org.jboss.aerogear.unifiedpush.cassandra.test.integration;

import static org.junit.Assert.assertTrue;

import java.time.Instant;
import java.util.UUID;

import org.jboss.aerogear.unifiedpush.utils.UUIDToDate;
import org.junit.Test;

import com.datastax.driver.core.utils.UUIDs;

public class UUIDToDateTest {

	@Test
	public void uuidToDateTest() {
		for (int i = 1; i < 100; i++) {
			long fromUUid = UUIDToDate.getTimeFromUUID(UUIDs.timeBased());
			long time = System.currentTimeMillis();

			// Assert take into account, that UUIDToDate.getTimeFromUUID might
			// delay in 1 millisecond
			assertTrue(time - fromUUid <= 1);
		}
	}

	@Test
	public void uuidShouldFail() {
		try {
			UUIDToDate.getTimeFromUUID(UUID.randomUUID());
			assertTrue("Should never pass", true);
		} catch (Exception e) {
			// Do nothing
		}
	}

	@Test
	public void uuidNull() {
		assertTrue(UUID.fromString("13814000-1dd2-11b2-8080-808080808080")
				.equals(UUIDs.startOf(Instant.EPOCH.toEpochMilli())));
	}
}
