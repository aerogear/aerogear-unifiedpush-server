package org.jboss.aerogear.unifiedpush.rest.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectReader;
import com.github.fge.jackson.JacksonUtils;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static java.text.MessageFormat.format;
import static org.junit.Assert.assertEquals;

/**
 */
public class VersionFilterTest {

    @Test
    public void generateDiff() throws IOException, JsonPatchException {
        String currentApi = "{\n" +
                "     \"alias\" : [\"someUsername\"],\n" +
                "     \"deviceType\" : [\"someDevice\"],\n" +
                "     \"categories\" : [\"someCategories\"],\n" +
                "     \"variants\" : [\"someVariantIDs\"],\n" +
                "     \"ttl\" : 3600,\n" +
                "     \"message\":\n" +
                "     {\n" +
                "       \"key\":\"value\",\n" +
                "       \"key2\":\"other value\",\n" +
                "       \"alert\":\"HELLO!\",\n" +
                "       \"action-category\":\"some value\",\n" +
                "       \"sound\":\"default\",\n" +
                "       \"badge\":2,\n" +
                "       \"content-available\" : true\n" +
                "     },\n" +
                "     \"simple-push\":\"version=123\"\n" +
                "   }";


        String newFormat = "{\n" +
                "  \"message\" : {\n" +
                "    \"alert\" : \"HELLO!\",\n" +
                "    \"action-category\" : \"some value\",\n" +
                "    \"sound\" : \"default\",\n" +
                "    \"badge\" : 2,\n" +
                "    \"content-available\" : true,\n" +
                "    \"data\" : {\n" +
                "      \"key\" : \"value\",\n" +
                "      \"key2\" : \"other value\"\n" +
                "    },\n" +
                "    \"simple-push\" : \"version=123\"\n" +
                "  },\n" +
                "  \"criteria\" : {\n" +
                "    \"alias\" : [ \"someUsername\" ],\n" +
                "    \"deviceType\" : [ \"someDevice\" ],\n" +
                "    \"categories\" : [ \"someCategories\" ],\n" +
                "    \"variants\" : [ \"someVariantIDs\" ]\n" +
                "  },\n" +
                "  \"config\" : {\n" +
                "    \"ttl\" : 3600\n" +
                "  }\n" +
                "}";

        ObjectReader reader = JacksonUtils.getReader();
        JsonNode current = reader.readTree(currentApi);
        JsonNode newNode = reader.readTree(newFormat);

        final JsonPatch patch = JsonPatch.fromJson(reader.readTree(getClass().getResourceAsStream("/100-latest.json")));
        JsonNode patched = patch.apply(current);

        final List<String> knownKeys = Arrays.asList("alert", "action-category", "sound", "badge", "content-available",
                "simple-push", "data");

        Iterator<Map.Entry<String, JsonNode>> nodeIterator = patched.get("message").fields();
        while (nodeIterator.hasNext()) {
            Map.Entry<String, JsonNode> entry = nodeIterator.next();

            if (!knownKeys.contains(entry.getKey())) {
                String json = format("['{'\"op\": \"move\",\"from\": \"/message/{0}\",\"path\": \"/message/data/{0}\"'}']", entry.getKey());
                patched = JsonPatch.fromJson(reader.readTree(json)).apply(patched);
            }
        }

        System.out.println(JacksonUtils.prettyPrint(patched));
        assertEquals(patched, newNode);
    }
}
