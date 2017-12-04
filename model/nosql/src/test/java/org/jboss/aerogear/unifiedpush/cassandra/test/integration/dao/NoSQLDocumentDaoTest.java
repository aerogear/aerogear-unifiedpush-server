package org.jboss.aerogear.unifiedpush.cassandra.test.integration.dao;

import static org.junit.Assert.assertEquals;

import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jboss.aerogear.unifiedpush.api.Alias;
import org.jboss.aerogear.unifiedpush.api.document.DocumentMetadata;
import org.jboss.aerogear.unifiedpush.api.document.QueryOptions;
import org.jboss.aerogear.unifiedpush.cassandra.CassandraConfig;
import org.jboss.aerogear.unifiedpush.cassandra.dao.AliasDao;
import org.jboss.aerogear.unifiedpush.cassandra.dao.DocumentDao;
import org.jboss.aerogear.unifiedpush.cassandra.dao.NullAlias;
import org.jboss.aerogear.unifiedpush.cassandra.dao.impl.DocumentKey;
import org.jboss.aerogear.unifiedpush.cassandra.dao.model.DocumentContent;
import org.jboss.aerogear.unifiedpush.cassandra.test.integration.FixedKeyspaceCreatingIntegrationTest;
import org.jboss.aerogear.unifiedpush.utils.UUIDToDate;
import org.junit.Assert;
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

		DocumentContent doc = documentDao.findOne(key).orElse(null);
		Assert.assertTrue(doc != null);

		documentDao.delete((DocumentContent) doc);

		doc = documentDao.findOne(key).orElse(null);
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
		DocumentContent doc1 = documentDao.findOne(key1).orElse(null);
		Assert.assertTrue(doc1 != null);

		// Search with existing snapshot
		DocumentContent doc2 = documentDao.findOne(key2).orElse(null);
		Assert.assertTrue(doc2 != null);

		// Search Without snapshot
		DocumentContent latest = documentDao.findOne(new DocumentKey(pushApplicationId, "STATUS")).orElse(null);

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
		DocumentContent doc1 = documentDao.findOne(key1).orElse(null);
		Assert.assertTrue(doc1.getDocumentId().equals("ID1"));

		// Search by complete key
		DocumentContent doc2 = documentDao.findOne(key2).orElse(null);
		Assert.assertTrue(doc2.getDocumentId().equals("ID2"));

		// Search when missing snapshot, return latest document regardless of
		// id.
		DocumentContent latest = documentDao.findOne(new DocumentKey(pushApplicationId, "STATUS")).orElse(null);
		Assert.assertTrue(doc2.getContent().equals(latest.getContent()));

		// Search by document id.
		DocumentContent doc1ById = documentDao.findOne(new DocumentKey(pushApplicationId, "STATUS"), "ID1");
		Assert.assertTrue(doc1.getContent().equals(doc1ById.getContent()));

		Stream<DocumentContent> documents = documentDao.find(new DocumentKey(pushApplicationId, "STATUS"),
				new QueryOptions("ID1"));
		Assert.assertTrue(documents.collect(Collectors.toList()).size() == 1);
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

		DocumentContent doc1 = documentDao.findOne(key1).orElse(null);
		DocumentContent doc2 = documentDao.findOne(key2).orElse(null);
		DocumentContent doc3 = documentDao.findOne(key3).orElse(null);
		DocumentContent doc4 = documentDao.findOne(key4).orElse(null);

		Assert.assertTrue(doc1 != null);
		Assert.assertTrue(doc2 != null);

		documentDao.delete(pushApplicationId);

		// Validate global documents were deleted
		doc1 = documentDao.findOne(key1).orElse(null);
		doc2 = documentDao.findOne(key2).orElse(null);
		Assert.assertTrue(doc1 == null);
		Assert.assertTrue(doc2 == null);

		// Validate alias documents were deleted
		doc3 = documentDao.findOne(key3).orElse(null);
		doc4 = documentDao.findOne(key4).orElse(null);
		Assert.assertTrue(doc3 == null);
		Assert.assertTrue(doc4 == null);
	}

	@Test
	public void testWithDateRange() {
		UUID pushApplicationId = UUID.randomUUID();

		try {
			// Create alias specific documents
			Alias alias1 = new Alias(pushApplicationId, UUIDs.timeBased(), "supprot@aerobase.org");
			Alias alias2 = new Alias(pushApplicationId, UUIDs.timeBased(), "supprot@aerobase.org");

			aliasDao.create(alias1);
			aliasDao.create(alias2);

			DocumentKey key1 = new DocumentKey(new DocumentMetadata(pushApplicationId.toString(), "STATUS", alias1));
			DocumentKey key2 = new DocumentKey(new DocumentMetadata(pushApplicationId.toString(), "STATUS", alias2));
			DocumentKey key3 = new DocumentKey(new DocumentMetadata(pushApplicationId.toString(), "STATUS", alias1));
			DocumentKey key4 = new DocumentKey(new DocumentMetadata(pushApplicationId.toString(), "STATUS", alias2));

			Long startTime = System.currentTimeMillis();

			// Create all documents
			Thread.sleep(100);
			documentDao.create(new DocumentContent(key1, "{TEST CONTENT 1}"));
			Thread.sleep(100);
			documentDao.create(new DocumentContent(key2, "{TEST CONTENT 2}"));

			Long midTime = System.currentTimeMillis();

			Thread.sleep(100);
			documentDao.create(new DocumentContent(key3, "{TEST CONTENT 3}"));
			Thread.sleep(100);
			documentDao.create(new DocumentContent(key4, "{TEST CONTENT 4}"));
			Thread.sleep(100);

			DocumentContent doc1 = documentDao.findOne(key1).orElse(null);
			DocumentContent doc2 = documentDao.findOne(key2).orElse(null);
			DocumentContent doc3 = documentDao.findOne(key3).orElse(null);
			DocumentContent doc4 = documentDao.findOne(key4).orElse(null);

			Assert.assertTrue(doc1 != null);
			Assert.assertTrue(doc2 != null);
			Assert.assertTrue(doc3 != null);
			Assert.assertTrue(doc4 != null);

			// Query 2 documents
			Assert.assertEquals(2, documentDao.find(key1, new QueryOptions(startTime, System.currentTimeMillis()))
					.collect(Collectors.toList()).size());

			// Query 1 documents
			Assert.assertEquals(1, documentDao.find(key2, new QueryOptions(midTime, System.currentTimeMillis()))
					.collect(Collectors.toList()).size());

			// Query 1 documents only by from date
			Assert.assertEquals(1,
					documentDao.find(key2, new QueryOptions(midTime)).collect(Collectors.toList()).size());

		} catch (Throwable e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void testWithLimit() {
		UUID pushApplicationId = UUID.randomUUID();

		try {
			// Create alias specific documents
			Alias alias1 = new Alias(pushApplicationId, UUIDs.timeBased(), "supprot@aerobase.org");

			aliasDao.create(alias1);

			DocumentKey key1 = new DocumentKey(new DocumentMetadata(pushApplicationId.toString(), "STATUS", alias1));
			DocumentKey key2 = new DocumentKey(new DocumentMetadata(pushApplicationId.toString(), "STATUS", alias1));
			DocumentKey key3 = new DocumentKey(new DocumentMetadata(pushApplicationId.toString(), "STATUS", alias1));
			DocumentKey key4 = new DocumentKey(new DocumentMetadata(pushApplicationId.toString(), "STATUS", alias1));

			// Create all documents
			Thread.sleep(100);
			documentDao.create(new DocumentContent(key1, "{TEST CONTENT 1}"));
			Thread.sleep(100);
			documentDao.create(new DocumentContent(key2, "{TEST CONTENT 2}"));

			Thread.sleep(100);
			documentDao.create(new DocumentContent(key3, "{TEST CONTENT 3}"));
			Thread.sleep(100);
			documentDao.create(new DocumentContent(key4, "{TEST CONTENT 4}"));
			Thread.sleep(100);

			// Query 2 documents limit
			Assert.assertTrue(documentDao.find(key1, new QueryOptions(null, null, null, 2)).collect(Collectors.toList())
					.size() == 2);

		} catch (Throwable e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void testDocumentIdLimit() {
		UUID pushApplicationId = UUID.randomUUID();

		try {
			// Create alias specific documents
			Alias alias1 = new Alias(pushApplicationId, UUIDs.timeBased(), "supprot@aerobase.org");
			aliasDao.create(alias1);

			DocumentKey key = new DocumentKey(new DocumentMetadata(pushApplicationId.toString(), "STATUS", alias1));
			UUID lowMark = null;
			UUID highMark = null;

			// Create 200 snapshots fir ID1
			for (int i = 0; i < 200; i++) {
				documentDao.create(new DocumentContent(key, "{TEST CONTENT " + i + "}", "ID1"));

				if (i == 10) {
					lowMark = key.getSnapshot();
				}

				if (i == 99) {
					highMark = key.getSnapshot();
				}

				// Recreate key to drop snapshot value
				key = new DocumentKey(new DocumentMetadata(pushApplicationId.toString(), "STATUS", alias1));
			}

			assertEquals(100, documentDao.find(key, new QueryOptions("ID1")).collect(Collectors.toList()).size());
			assertEquals(200, documentDao.find(key, new QueryOptions("ID1", 205)).collect(Collectors.toList()).size());
			assertEquals(90,
					documentDao
							.find(key,
									new QueryOptions(UUIDToDate.getTimeFromUUID(lowMark),
											UUIDToDate.getTimeFromUUID(highMark), "ID1", 205))
							.collect(Collectors.toList()).size());

		} catch (Throwable e) {
			Assert.fail(e.getMessage());
		}
	}
}
