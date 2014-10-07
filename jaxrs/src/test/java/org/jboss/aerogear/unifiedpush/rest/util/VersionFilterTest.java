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
        ObjectReader reader = JacksonUtils.getReader();
        JsonNode current = reader.readTree(getClass().getResourceAsStream("/message-format-100.json"));
        JsonNode newNode = reader.readTree(getClass().getResourceAsStream("/new-message-format.json"));

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
