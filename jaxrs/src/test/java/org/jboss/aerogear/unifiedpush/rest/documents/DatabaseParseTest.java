package org.jboss.aerogear.unifiedpush.rest.documents;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.UUID;

import org.jboss.aerogear.unifiedpush.api.Installation;
import org.jboss.aerogear.unifiedpush.cassandra.dao.impl.DocumentKey;
import org.jboss.aerogear.unifiedpush.cassandra.dao.model.DocumentContent;
import org.jboss.aerogear.unifiedpush.cassandra.dao.model.parser.JsonDocumentContent;
import org.jboss.aerogear.unifiedpush.rest.RestEndpointTest;
import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class DatabaseParseTest extends RestEndpointTest {

	@Test
	public void testDocumentContent() {
		ObjectMapper mapper = new ObjectMapper();
		Installation exampleObject = RestEndpointTest.getIosDefaultInstallation();

		DocumentContent content = null;

		try {
			content = new JsonDocumentContent(new DocumentKey(UUID.randomUUID(), "DEVICES"),
					mapper.writeValueAsString(exampleObject));
			content.setDocumentId(UUID.randomUUID().toString());
		} catch (JsonProcessingException e) {
			Assert.fail("Error while parsing device Installation !!!");
		}

		try {
			String value = mapper.writeValueAsString(content);
			assertThat(value.contains("content\":{")).isTrue();

			DocumentContent content2 = mapper.readValue(value, JsonDocumentContent.class);
			assertThat(content2.getContent().equals(content.getContent())).isTrue();
		} catch (JsonProcessingException e) {
			Assert.fail("Error while parsing DocumentContent !!!");
		} catch (IOException e) {
			Assert.fail("Error while reading DocumentContent !!!");
		}

	}
}
