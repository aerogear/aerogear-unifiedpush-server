package org.jboss.aerogear.unifiedpush.cassandra.test.integration.dao;

import java.util.List;
import java.util.UUID;

import org.jboss.aerogear.unifiedpush.api.Alias;
import org.jboss.aerogear.unifiedpush.cassandra.dao.AliasDao;
import org.jboss.aerogear.unifiedpush.cassandra.dao.CassandraConfig;
import org.jboss.aerogear.unifiedpush.cassandra.dao.model.User;
import org.jboss.aerogear.unifiedpush.cassandra.test.integration.FixedKeyspaceCreatingIntegrationTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.SimpleKey;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.Assert;

import com.datastax.driver.core.utils.UUIDs;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = CassandraConfig.class)
public class NoSQLAliasDaoTest extends FixedKeyspaceCreatingIntegrationTest {
	public static final String TEST_EMAIL = "TEST@XXX.COM";
	public static final String TEST_PHONE = "16541237890";
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
	// Create Simple user from alias.
	public void testCreateUser() {
		Alias alias = new Alias(UUID.randomUUID(), UUIDs.timeBased(), TEST_EMAIL);
		aliasDao.create(alias);

		Assert.isTrue(aliasDao.findAll(alias.getPushApplicationId()).size() == 2);
	}

	@Test
	public void testDeleteUser() {
		String aliasx = "111@xxx.com";
		Alias alias1 = new Alias(UUID.randomUUID(), UUIDs.timeBased(), aliasx);

		// Crate user
		aliasDao.create(alias1);

		// Crate user again record
		List<User> userList = aliasDao.create(alias1);

		// Query findOne return latest user
		User user1 = aliasDao.findOne(userList.stream().findFirst().get().getKey());
		Assert.isTrue(user1.getAlias().equals(aliasx));

		// Query latest alias by alias name
		Alias aliasFromDb = aliasDao.findByAlias(alias1.getPushApplicationId(), aliasx);
		Assert.isTrue(aliasFromDb != null);

		// Remove all users by alias
		aliasDao.remove(alias1.getPushApplicationId(), aliasx);

		Alias deletedFromDb = aliasDao.findByAlias(alias1.getPushApplicationId(), aliasx);
		Assert.isTrue(deletedFromDb == null);
	}

	@Test
	public void testUsersCache() {
		Alias alias = new Alias(UUID.randomUUID(), UUIDs.timeBased(), TEST_CACHE);
		aliasDao.create(alias);
		aliasDao.findByAlias(alias.getPushApplicationId(), TEST_CACHE);

		Cache cache = cacheManager.getCache(AliasDao.CACHE_NAME);
		Assert.isTrue(cache.get(new SimpleKey(alias.getPushApplicationId(), TEST_CACHE)) != null);
	}

	@Test
	public void testDeleteAll() {
		UUID pushApplicationId = UUID.randomUUID();

		createTestUsers(pushApplicationId, TEST_EMAIL);

		Assert.isTrue(aliasDao.findAll(pushApplicationId).size() == 5);
		aliasDao.removeAll(pushApplicationId);
		Assert.isTrue(aliasDao.findAll(pushApplicationId).size() == 0);

		createTestUsers(pushApplicationId, TEST_EMAIL);
		Assert.isTrue(aliasDao.findAll(pushApplicationId).size() == 5);
		aliasDao.remove(pushApplicationId, TEST_EMAIL);
		aliasDao.remove(pushApplicationId, TEST_PHONE);
		Assert.isTrue(aliasDao.findAll(pushApplicationId).size() == 0);
	}

	@Test
	public void testFindLatestAlias() {
		UUID pushApplicationId1 = UUID.randomUUID();
		createTestUsers(pushApplicationId1, "1" + TEST_EMAIL);

		UUID pushApplicationId2 = UUID.randomUUID();
		createTestUsers(pushApplicationId2, "2" + TEST_EMAIL);

		Alias alias = aliasDao.findByAlias(null, TEST_PHONE);
		Assert.isTrue(alias.getPushApplicationId().equals(pushApplicationId2));

		// Delete aliases from application2 only
		aliasDao.remove(pushApplicationId2, "2" + TEST_EMAIL);
		aliasDao.remove(pushApplicationId2, TEST_PHONE);

		alias = aliasDao.findByAlias(null, TEST_PHONE);
		Assert.isTrue(alias.getPushApplicationId().equals(pushApplicationId1));
	}

	private void createTestUsers(UUID pushApplicationId, String email) {
		Alias alias1 = new Alias(pushApplicationId, UUIDs.timeBased(), email);
		Alias alias2 = new Alias(pushApplicationId, UUIDs.timeBased(), email);

		alias2.setMobile(TEST_PHONE);

		// Create two users
		aliasDao.create(alias1);
		aliasDao.create(alias2);
	}
}
