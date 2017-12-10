package org.jboss.aerogear.unifiedpush.cassandra.test.integration.dao;

import java.util.UUID;

import org.jboss.aerogear.unifiedpush.cassandra.CassandraConfig;
import org.jboss.aerogear.unifiedpush.cassandra.dao.OtpCodeDao;
import org.jboss.aerogear.unifiedpush.cassandra.dao.model.OtpCode;
import org.jboss.aerogear.unifiedpush.cassandra.dao.model.OtpCodeKey;
import org.jboss.aerogear.unifiedpush.cassandra.test.integration.FixedKeyspaceCreatingIntegrationTest;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.cassandra.core.InsertOptions;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = CassandraConfig.class)
public class NoSQLOtpCodeDaoTest extends FixedKeyspaceCreatingIntegrationTest {

	@Autowired
	private OtpCodeDao codeDao;

	// Create Simple otp code.
	@Test
	public void testCreateCode() {
		UUID variantId = UUID.randomUUID();
		OtpCodeKey key = new OtpCodeKey(variantId, UUID.randomUUID().toString(), "123456");
		codeDao.save(new OtpCode(key));
	}

	// Create Simple otp code.
	@Test
	public void testCreateWithTTL() {
		UUID variantId = UUID.randomUUID();
		OtpCodeKey key = new OtpCodeKey(variantId, UUID.randomUUID().toString(), "123456");
		InsertOptions options = InsertOptions.builder().ttl(2).build();
		OtpCode code1 = new OtpCode(key);
		codeDao.save(code1, options);

		OtpCode code2 = codeDao.findById(key).orElse(null);
		Assert.assertTrue(code2 != null);
		Assert.assertTrue(code2.getKey().getCode().equals(code1.getKey().getCode()));

		// Wait to TTL + 2 sec
		try {
			Thread.sleep(4000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		OtpCode code3 = codeDao.findById(key).orElse(null);
		Assert.assertTrue(code3 == null);
	}

	@Test
	public void testMultipleCodesWith() {
		UUID variantId = UUID.randomUUID();
		UUID deviceToken = UUID.randomUUID();

		OtpCodeKey key1 = new OtpCodeKey(variantId, deviceToken.toString(), "12345");
		OtpCodeKey key2 = new OtpCodeKey(variantId, deviceToken.toString(), "67890");

		codeDao.save(new OtpCode(key1));
		codeDao.save(new OtpCode(key2));

		OtpCode code1 = codeDao.findById(key1).orElse(null);
		Assert.assertTrue(code1 != null);

		codeDao.deleteAll(new OtpCodeKey(variantId, deviceToken.toString(), null));

		OtpCode code2 = codeDao.findById(key1).orElse(null);
		Assert.assertTrue(code2 == null);

		OtpCode code3 = codeDao.findById(key2).orElse(null);
		Assert.assertTrue(code3 == null);
	}

}