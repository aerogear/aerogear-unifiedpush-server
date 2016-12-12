package org.jboss.aerogear.unifiedpush.cassandra.test.integration.dao;

import java.util.UUID;

import org.jboss.aerogear.unifiedpush.api.DocumentMetadata;
import org.jboss.aerogear.unifiedpush.api.IDocument;
import org.jboss.aerogear.unifiedpush.cassandra.dao.CassandraConfig;
import org.jboss.aerogear.unifiedpush.cassandra.dao.impl.DocumentKey;
import org.jboss.aerogear.unifiedpush.cassandra.dao.model.DocumentContent;
import org.jboss.aerogear.unifiedpush.cassandra.test.integration.FixedKeyspaceCreatingIntegrationTest;
import org.jboss.aerogear.unifiedpush.dao.DocumentDao;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = CassandraConfig.class)
public class NoSQLDocumentDaoTest extends FixedKeyspaceCreatingIntegrationTest {

	@Autowired
	private DocumentDao<DocumentContent, DocumentKey> documentDao;

	@Before
	public void setupTemplate() {
		execute("cassandra-test-cql-dataload.cql", this.keyspace);
	}

	@Test
	public void testCreateGlobalDocuemnt() {
		DocumentKey key = new DocumentKey(new DocumentMetadata(UUID.randomUUID().toString(), "STATUS"));
		documentDao.create(new DocumentContent(key, "{TEST CONTENT}"));
		Assert.assertTrue(true);
	}

	@Test
	public void testDeleteGlobalDocuemnt() {
		DocumentKey key = new DocumentKey(new DocumentMetadata(UUID.randomUUID().toString(), "STATUS"));
		documentDao.create(new DocumentContent(key, "{TEST CONTENT}"));

		IDocument<DocumentKey> doc = documentDao.findOne(key);
		Assert.assertTrue(doc != null);

		documentDao.delete((DocumentContent)doc);

		doc = documentDao.findOne(key);
		Assert.assertTrue(doc == null);
	}

	@Test
	public void testGlobalDocuemnt() {
		UUID pushApplicationId = UUID.randomUUID();

		DocumentKey key1 = new DocumentKey(new DocumentMetadata(pushApplicationId.toString(), "STATUS"));
		DocumentKey key2 = new DocumentKey(new DocumentMetadata(pushApplicationId.toString(), "STATUS"));
		documentDao.create(new DocumentContent(key1, "{TEST CONTENT 1}"));
		documentDao.create(new DocumentContent(key2, "{TEST CONTENT 2}"));

		// Search with existing snapshot
		IDocument<DocumentKey> doc1 = documentDao.findOne(key1);
		Assert.assertTrue(doc1 != null);

		// Search with existing snapshot
		IDocument<DocumentKey> doc2 = documentDao.findOne(key2);
		Assert.assertTrue(doc2 != null);

		// Search Without snapshot
		IDocument<DocumentKey> latest = documentDao.findOne(new DocumentKey(pushApplicationId, "STATUS"));

		Assert.assertTrue(doc2.getContent().equals(latest.getContent()));
	}

	@Test
	public void testLatestGlobalDocuemntWithId() {
		UUID pushApplicationId = UUID.randomUUID();

		DocumentKey key1 = new DocumentKey(new DocumentMetadata(pushApplicationId.toString(), "STATUS"));
		DocumentKey key2 = new DocumentKey(new DocumentMetadata(pushApplicationId.toString(), "STATUS"));
		documentDao.create(new DocumentContent(key1, "{TEST CONTENT 1}", "ID1"));
		documentDao.create(new DocumentContent(key2, "{TEST CONTENT 2}", "ID2"));

		// Search by complete key
		IDocument<DocumentKey> doc1 = documentDao.findOne(key1);
		Assert.assertTrue(doc1.getDocumentId().equals("ID1"));

		// Search by complete key
		IDocument<DocumentKey> doc2 = documentDao.findOne(key2);
		Assert.assertTrue(doc2.getDocumentId().equals("ID2"));

		// Search when missing snapshot, return latest document regardless of id.
		IDocument<DocumentKey> latest = documentDao.findOne(new DocumentKey(pushApplicationId, "STATUS"));
		Assert.assertTrue(doc2.getContent().equals(latest.getContent()));

		// Search by document id.
		IDocument<DocumentKey> doc1ById = documentDao.findOne(new DocumentKey(pushApplicationId, "STATUS"), "ID1");
		Assert.assertTrue(doc1.getContent().equals(doc1ById.getContent()));
	}


	@Test
	public void testDeleteApplicaitonDocuemnts() {
		UUID pushApplicationId = UUID.randomUUID();

		DocumentKey key1 = new DocumentKey(new DocumentMetadata(pushApplicationId.toString(), "STATUS"));
		DocumentKey key2 = new DocumentKey(new DocumentMetadata(pushApplicationId.toString(), "STATUS"));
		documentDao.create(new DocumentContent(key1, "{TEST CONTENT 1}"));
		documentDao.create(new DocumentContent(key2, "{TEST CONTENT 2}"));

		IDocument<DocumentKey> doc1 = documentDao.findOne(key1);
		Assert.assertTrue(doc1 != null);

		IDocument<DocumentKey> doc2 = documentDao.findOne(key2);
		Assert.assertTrue(doc2 != null);

		documentDao.delete(pushApplicationId);

		doc1 = documentDao.findOne(key1);
		Assert.assertTrue(doc1 == null);

		doc2 = documentDao.findOne(key2);
		Assert.assertTrue(doc2 == null);
	}

}
