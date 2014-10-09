package org.jboss.aerogear.unifiedpush.rest.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectReader;
import com.github.fge.jackson.JacksonUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 */
public class RequestTransformerTest {
    private RequestTransformer requestTransformer = new RequestTransformer();

    @Test
    public void shouldTransformSenderRequest() throws IOException {
        //given
        ObjectReader reader = JacksonUtils.getReader();
        final String json = IOUtils.toString(getClass().getResourceAsStream("/message-format-100.json"));
        StringBuilder current = new StringBuilder(json);

        //when
        final StringBuilder patched = requestTransformer.transform("/rest/sender", "100", current);

        //then
        final JsonNode patchedNode = reader.readTree(patched.toString());
        JsonNode newNode = reader.readTree(getClass().getResourceAsStream("/new-message-format.json"));

        assertEquals(newNode, patchedNode);
    }
}
