package org.jboss.aerogear.unifiedpush.cassandra.test.integration.dao;

import java.util.UUID;

import org.jboss.aerogear.unifiedpush.api.Alias;
import org.jboss.aerogear.unifiedpush.api.DocumentMetadata;
import org.jboss.aerogear.unifiedpush.api.IDocument;
import org.jboss.aerogear.unifiedpush.cassandra.dao.AliasDao;
import org.jboss.aerogear.unifiedpush.cassandra.dao.CassandraConfig;
import org.jboss.aerogear.unifiedpush.cassandra.dao.NullAlias;
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

import com.datastax.driver.core.utils.UUIDs;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = CassandraConfig.class)
public class NoSQLDocumentDaoTest extends FixedKeyspaceCreatingIntegrationTest {

	@Autowired
	private DocumentDao<DocumentContent, DocumentKey> documentDao;

	@Autowired
	private AliasDao aliasDao;

	@Before
	public void setupTemplate() {
		execute("cassandra-test-cql-dataload.cql", this.keyspace);
	}

	@Test
	public void testCreateGlobalDocuemnt() {
		UUID pushApplicationId = UUID.randomUUID();

		DocumentKey key = new DocumentKey(
				new DocumentMetadata(pushApplicationId, "STATUS", NullAlias.getAlias(pushApplicationId)));
		documentDao.create(new DocumentContent(key, "{TEST CONTENT}"));
		Assert.assertTrue(true);
	}

	@Test
	public void testDeleteGlobalDocuemnt() {
		UUID pushApplicationId = UUID.randomUUID();

		DocumentKey key = new DocumentKey(
				new DocumentMetadata(pushApplicationId, "STATUS", NullAlias.getAlias(pushApplicationId)));
		documentDao.create(new DocumentContent(key, "{TEST CONTENT}"));

		IDocument<DocumentKey> doc = documentDao.findOne(key);
		Assert.assertTrue(doc != null);

		documentDao.delete((DocumentContent) doc);

		doc = documentDao.findOne(key);
		Assert.assertTrue(doc == null);
	}

	@Test
	public void testGlobalDocuemnt() {
		UUID pushApplicationId = UUID.randomUUID();

		DocumentKey key1 = new DocumentKey(
				new DocumentMetadata(pushApplicationId, "STATUS", NullAlias.getAlias(pushApplicationId)));
		DocumentKey key2 = new DocumentKey(
				new DocumentMetadata(pushApplicationId, "STATUS", NullAlias.getAlias(pushApplicationId)));
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

		DocumentKey key1 = new DocumentKey(
				new DocumentMetadata(pushApplicationId, "STATUS", NullAlias.getAlias(pushApplicationId)));
		DocumentKey key2 = new DocumentKey(
				new DocumentMetadata(pushApplicationId, "STATUS", NullAlias.getAlias(pushApplicationId)));
		documentDao.create(new DocumentContent(key1, "{TEST CONTENT 1}", "ID1"));
		documentDao.create(new DocumentContent(key2, "{TEST CONTENT 2}", "ID2"));

		// Search by complete key
		IDocument<DocumentKey> doc1 = documentDao.findOne(key1);
		Assert.assertTrue(doc1.getDocumentId().equals("ID1"));

		// Search by complete key
		IDocument<DocumentKey> doc2 = documentDao.findOne(key2);
		Assert.assertTrue(doc2.getDocumentId().equals("ID2"));

		// Search when missing snapshot, return latest document regardless of
		// id.
		IDocument<DocumentKey> latest = documentDao.findOne(new DocumentKey(pushApplicationId, "STATUS"));
		Assert.assertTrue(doc2.getContent().equals(latest.getContent()));

		// Search by document id.
		IDocument<DocumentKey> doc1ById = documentDao.findOne(new DocumentKey(pushApplicationId, "STATUS"), "ID1");
		Assert.assertTrue(doc1.getContent().equals(doc1ById.getContent()));
	}

	@Test
	public void testDeleteApplicaitonDocuemnts() {
		UUID pushApplicationId = UUID.randomUUID();

		// Create global documents
		DocumentKey key1 = new DocumentKey(
				new DocumentMetadata(pushApplicationId, "STATUS", NullAlias.getAlias(pushApplicationId)));
		DocumentKey key2 = new DocumentKey(
				new DocumentMetadata(pushApplicationId, "STATUS", NullAlias.getAlias(pushApplicationId)));

		// Create alias specific documents
		Alias alias1 = new Alias(pushApplicationId, UUIDs.timeBased(), "supprot@aerobase.org");
		Alias alias2 = new Alias(pushApplicationId, UUIDs.timeBased(), "supprot@aerobase.org");

		aliasDao.create(alias1);
		aliasDao.create(alias2);

		DocumentKey key3 = new DocumentKey(new DocumentMetadata(pushApplicationId.toString(), "STATUS", alias1));
		DocumentKey key4 = new DocumentKey(new DocumentMetadata(pushApplicationId.toString(), "STATUS", alias2));

		// Create all documents
		documentDao.create(new DocumentContent(key1, "{TEST CONTENT 1}"));
		documentDao.create(new DocumentContent(key2, "{TEST CONTENT 2}"));
		documentDao.create(new DocumentContent(key3, "{TEST CONTENT 3}"));
		documentDao.create(new DocumentContent(key4, "{TEST CONTENT 4}"));

		IDocument<DocumentKey> doc1 = documentDao.findOne(key1);
		IDocument<DocumentKey> doc2 = documentDao.findOne(key2);
		IDocument<DocumentKey> doc3 = documentDao.findOne(key3);
		IDocument<DocumentKey> doc4 = documentDao.findOne(key4);

		Assert.assertTrue(doc1 != null);
		Assert.assertTrue(doc2 != null);

		documentDao.delete(pushApplicationId);

		// Validate global documents were deleted
		doc1 = documentDao.findOne(key1);
		doc2 = documentDao.findOne(key2);
		Assert.assertTrue(doc1 == null);
		Assert.assertTrue(doc2 == null);

		// Validate alias documents were deleted
		doc3 = documentDao.findOne(key3);
		doc4 = documentDao.findOne(key4);
		Assert.assertTrue(doc3 == null);
		Assert.assertTrue(doc4 == null);
	}

}
