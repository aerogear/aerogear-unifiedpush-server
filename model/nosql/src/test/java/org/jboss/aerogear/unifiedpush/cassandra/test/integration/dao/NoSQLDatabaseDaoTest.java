package org.jboss.aerogear.unifiedpush.cassandra.test.integration.dao;

import java.util.UUID;

import org.jboss.aerogear.unifiedpush.cassandra.CassandraConfig;
import org.jboss.aerogear.unifiedpush.cassandra.dao.DatabaseDao;
import org.jboss.aerogear.unifiedpush.cassandra.dao.model.Database;
import org.jboss.aerogear.unifiedpush.cassandra.dao.model.DatabaseQueryKey;
import org.jboss.aerogear.unifiedpush.cassandra.test.integration.FixedKeyspaceCreatingIntegrationTest;
import org.junit.Assert;
import org.junit.Before;
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

	@Before
	public void setupTemplate() {
		execute("cassandra-test-cql-dataload.cql", this.keyspace);
	}

	@Test
	public void testDatabaseCache() {
		Database db = new Database(UUID.randomUUID(), "SETTINGS");
		databaseDao.create(db);
		databaseDao.findOne(new DatabaseQueryKey(db));

		Cache cache = cacheManager.getCache(DatabaseDao.CACHE_NAME);
		Assert.assertTrue(cache.get(new DatabaseQueryKey(db)) != null);
	}

}
