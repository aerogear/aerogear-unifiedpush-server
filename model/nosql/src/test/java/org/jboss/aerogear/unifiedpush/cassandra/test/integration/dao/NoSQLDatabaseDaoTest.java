package org.jboss.aerogear.unifiedpush.cassandra.test.integration.dao;

import java.util.UUID;
import java.util.stream.Collectors;

import org.jboss.aerogear.unifiedpush.cassandra.CassandraConfig;
import org.jboss.aerogear.unifiedpush.cassandra.dao.DatabaseDao;
import org.jboss.aerogear.unifiedpush.cassandra.dao.model.Database;
import org.jboss.aerogear.unifiedpush.cassandra.dao.model.DatabaseQueryKey;
import org.jboss.aerogear.unifiedpush.cassandra.test.integration.FixedKeyspaceCreatingIntegrationTest;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = CassandraConfig.class)
public class NoSQLDatabaseDaoTest extends FixedKeyspaceCreatingIntegrationTest {

	@Autowired
	private DatabaseDao databaseDao;
	@Autowired
	private CacheManager cacheManager;


	@Test
	public void testDatabaseCache() {
		Database db = new Database(UUID.randomUUID(), "SETTINGS");
		databaseDao.create(db);
		databaseDao.findOne(new DatabaseQueryKey(db));

		Cache cache = cacheManager.getCache(DatabaseDao.CACHE_NAME);
		Assert.assertTrue(cache.get(new DatabaseQueryKey(db)) != null);
	}

	@Test
	public void testDatabaseForApplication() {
		Database db1 = new Database(UUID.randomUUID(), "SETTINGS1");
		// Database snapshot are Date based, make sure its not the same date
		sleepSilently(1);

		Database db2 = new Database(db1.getKey().getPushApplicationId(), "SETTINGS2");

		databaseDao.create(db1);
		databaseDao.create(db2);

		Assert.assertEquals(2, databaseDao.find(db1.getKey().getPushApplicationId()).collect(Collectors.toList()).size());
	}

}
