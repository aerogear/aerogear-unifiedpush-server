/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
