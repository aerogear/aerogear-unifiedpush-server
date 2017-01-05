package org.jboss.aerogear.unifiedpush.cassandra.test.integration.dao;

import java.util.UUID;

import org.jboss.aerogear.unifiedpush.api.Alias;
import org.jboss.aerogear.unifiedpush.cassandra.dao.AliasDao;
import org.jboss.aerogear.unifiedpush.cassandra.dao.CassandraConfig;
import org.jboss.aerogear.unifiedpush.cassandra.test.integration.FixedKeyspaceCreatingIntegrationTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.Assert;

import com.datastax.driver.core.utils.UUIDs;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = CassandraConfig.class)
public class NoSQLAliasDaoTest extends FixedKeyspaceCreatingIntegrationTest {
	public static final String TEST_EMAIL = "TEST@XXX.COM";
	public static final String TEST_CACHE = "CACHE@XXX.COM";

	@Autowired
	private AliasDao aliasDao;
	@Autowired
	private CacheManager cacheManager;

	@Before
	public void setupTemplate() {
		execute("cassandra-test-cql-dataload.cql", this.keyspace);
	}

	@Test
	public void testCreateUser() {
		Alias alias = new Alias(UUID.randomUUID(), UUIDs.timeBased(), TEST_EMAIL);
		aliasDao.create(alias);
	}

	@Test
	public void testDeleteUser() {
		Alias alias = new Alias(UUID.randomUUID(), UUIDs.timeBased(), TEST_EMAIL);
		aliasDao.create(alias);

		Alias aliasFromDb = aliasDao.findByAlias(TEST_EMAIL);
		Assert.isTrue(aliasFromDb != null);

		aliasDao.delete(aliasFromDb);
		Alias deletedFromDb = aliasDao.find(aliasFromDb);
		Assert.isTrue(deletedFromDb == null);
	}

	@Test
	public void testUsersCache() {
		Alias alias = new Alias(UUID.randomUUID(), UUIDs.timeBased(), TEST_CACHE);
		aliasDao.create(alias);
		aliasDao.findByAlias(TEST_CACHE);

		Cache cache = cacheManager.getCache(AliasDao.CACHE_NAME);
		Assert.isTrue(cache.get(TEST_CACHE) != null);
	}

	@Test
	public void testDeleteAll() {
		UUID id = UUID.randomUUID();
		Alias alias1 = new Alias(id, UUIDs.timeBased(), TEST_EMAIL);
		Alias alias2 = new Alias(id, UUIDs.timeBased(), TEST_EMAIL);

		// Create two users
		aliasDao.create(alias1);
		aliasDao.create(alias2);
		Assert.isTrue(aliasDao.findAll(id).size() >= 2);

		// Delete by application
		aliasDao.findAll(id).forEach((user) -> {
			aliasDao.delete(user);
		});

		Assert.isTrue(aliasDao.findAll(id).size() == 0);
	}
}
