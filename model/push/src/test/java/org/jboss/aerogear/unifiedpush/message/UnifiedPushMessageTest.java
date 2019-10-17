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
package org.jboss.aerogear.unifiedpush.message;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class UnifiedPushMessageTest {

    @Test
    public void shouldSerializeMessage() throws IOException {
        //when
        UnifiedPushMessage unifiedPushMessage = new UnifiedPushMessage();

        Message message = new Message();

        message.setAlert("HELLO!");
        message.getApns().setActionCategory("some value");
        message.setSound("default");
        message.setBadge(2);
        message.getApns().setContentAvailable(true);

        final HashMap<String, Object> data = new HashMap<>();
        data.put("key", "value");
        data.put("key2", "other value");
        message.setUserData(data);

        message.setSimplePush("version=123");
        unifiedPushMessage.setMessage(message);

        final Criteria criteria = new Criteria();
        criteria.setAliases(Arrays.asList("someUsername"));
        criteria.setDeviceTypes(Arrays.asList("someDevice"));
        criteria.setCategories(Arrays.asList("someCategories"));
        criteria.setVariants(Arrays.asList("someVariantIDs"));
        unifiedPushMessage.setCriteria(criteria);

        final Config config = new Config();
        config.setTimeToLive(3360);
        unifiedPushMessage.setConfig(config);

        //then
        final ObjectMapper mapper = new ObjectMapper();
        final JsonNode value = mapper.valueToTree(unifiedPushMessage);

        JsonNode format = mapper.reader().readTree(getClass().getResourceAsStream("/message-format.json"));
        assertEquals(format, value);
    }

    @Test
    public void testPriorityMapping() throws IOException {
        final ObjectMapper mapper = new ObjectMapper();
        Message message = mapper.readValue(getClass().getResourceAsStream("/message-normal-priority.json"), UnifiedPushMessage.class).getMessage();
        
        assertEquals(Priority.NORMAL, message.getPriority());
    }
    
    @Test
    public void testPriorityEnumHighMapping() throws IOException {
        final ObjectMapper mapper = new ObjectMapper();        
        Message message = mapper.readValue(getClass().getResourceAsStream("/message-high-priority.json"), UnifiedPushMessage.class).getMessage();
        
        assertEquals(Priority.HIGH, message.getPriority());
    }
    
    @Test
    public void createBroadcastMessage() throws IOException {

        final Map<String, Object> container = new LinkedHashMap<>();
        final Map<String, Object> messageObject = new LinkedHashMap<>();

        messageObject.put("alert", "Howdy");
        messageObject.put("sound", "default");
        messageObject.put("badge", 2);
        Map<String, String> data = new HashMap<>();
        data.put("someKey", "someValue");
        messageObject.put("user-data", data);

        container.put("message", messageObject);

        // parse it:

        final UnifiedPushMessage unifiedPushMessage = parsePushMessage(container);

        assertEquals("Howdy", unifiedPushMessage.getMessage().getAlert());
        assertEquals("default", unifiedPushMessage.getMessage().getSound());
        assertEquals(2, unifiedPushMessage.getMessage().getBadge());
        assertEquals("someValue", unifiedPushMessage.getMessage().getUserData().get("someKey"));

        // no TTL:
        assertEquals(-1, unifiedPushMessage.getConfig().getTimeToLive());

        // multiple access?
        assertEquals("Howdy", unifiedPushMessage.getMessage().getAlert());
        assertEquals("someValue", unifiedPushMessage.getMessage().getUserData().get("someKey"));

        assertNull(unifiedPushMessage.getCriteria().getAliases());
        assertNull(unifiedPushMessage.getCriteria().getDeviceTypes());
        assertNull(unifiedPushMessage.getCriteria().getCategories());
        assertNull(unifiedPushMessage.getCriteria().getVariants());
        assertNull(unifiedPushMessage.getMessage().getSimplePush());
    }

    @Test
    public void createBroadcastMessageWithSimplePush() throws IOException {

        final Map<String, Object> container = new LinkedHashMap<>();
        final Map<String, Object> messageObject = new LinkedHashMap<>();

        messageObject.put("alert", "Howdy");
        messageObject.put("sound", "default");
        messageObject.put("badge", 2);
        Map<String, String> data = new HashMap<>();
        data.put("someKey", "someValue");
        messageObject.put("user-data", data);

        container.put("message", messageObject);
        messageObject.put("simple-push", "version=123");

        // parse it:
        final UnifiedPushMessage unifiedPushMessage = parsePushMessage(container);

        assertEquals("Howdy", unifiedPushMessage.getMessage().getAlert());
        assertEquals("default", unifiedPushMessage.getMessage().getSound());
        assertEquals(2, unifiedPushMessage.getMessage().getBadge());
        assertEquals("someValue", unifiedPushMessage.getMessage().getUserData().get("someKey"));

        // multiple access?
        assertEquals("Howdy", unifiedPushMessage.getMessage().getAlert());
        assertEquals("someValue", unifiedPushMessage.getMessage().getUserData().get("someKey"));

        assertNull(unifiedPushMessage.getCriteria().getAliases());
        assertNull(unifiedPushMessage.getCriteria().getDeviceTypes());
        assertNull(unifiedPushMessage.getCriteria().getCategories());
        assertNull(unifiedPushMessage.getCriteria().getVariants());
        assertEquals("version=123", unifiedPushMessage.getMessage().getSimplePush());
    }

    @Test(expected = UnrecognizedPropertyException.class)
    public void createBroadcastMessageWithIncorrectSimplePush() throws IOException {

        final Map<String, Object> container = new LinkedHashMap<>();
        final Map<String, Object> messageObject = new LinkedHashMap<>();

        messageObject.put("alert", "Howdy");
        messageObject.put("sound", "default");
        messageObject.put("badge", 2);
        Map<String, String> data = new HashMap<>();
        data.put("someKey", "someValue");
        messageObject.put("user-data", data);

        container.put("message", messageObject);
        messageObject.put("simplePush", "version=123");

        // parse it:
        parsePushMessage(container);
    }

    @Test
    public void noBadgePayload() throws IOException {

        final Map<String, Object> container = new LinkedHashMap<>();
        final Map<String, Object> messageObject = new LinkedHashMap<>();

        messageObject.put("alert", "Howdy");
        messageObject.put("sound", "default");
        Map<String, String> data = new HashMap<>();
        data.put("someKey", "someValue");
        messageObject.put("user-data", data);

        container.put("message", messageObject);

        // parse it:
        final UnifiedPushMessage unifiedPushMessage = parsePushMessage(container);

        assertEquals("Howdy", unifiedPushMessage.getMessage().getAlert());
        assertEquals(-1, unifiedPushMessage.getMessage().getBadge());
    }

    @Test
    public void contentAvailable() throws IOException {

        final Map<String, Object> container = new LinkedHashMap<>();
        final Map<String, Object> messageObject = new LinkedHashMap<>();
        final Map<String, Object> apnsObject = new LinkedHashMap<>();

        messageObject.put("alert", "Howdy");
        messageObject.put("sound", "default");

        Map<String, String> data = new HashMap<>();
        data.put("someKey", "someValue");
        messageObject.put("user-data", data);

        apnsObject .put("content-available", true);
        messageObject.put("apns",apnsObject );
        container.put("message", messageObject);

        // parse it:
        final UnifiedPushMessage unifiedPushMessage = parsePushMessage(container);

        assertEquals("Howdy", unifiedPushMessage.getMessage().getAlert());
        assertEquals(-1, unifiedPushMessage.getMessage().getBadge());
        assertTrue(unifiedPushMessage.getMessage().getApns().isContentAvailable());
    }

    @Test
    public void noContentAvailable() throws IOException {

        final Map<String, Object> container = new LinkedHashMap<>();
        final Map<String, Object> messageObject = new LinkedHashMap<>();

        messageObject.put("alert", "Howdy");
        messageObject.put("sound", "default");
        Map<String, String> data = new HashMap<>();
        data.put("someKey", "someValue");
        messageObject.put("user-data", data);

        container.put("message", messageObject);

        // parse it:
        final UnifiedPushMessage unifiedPushMessage = parsePushMessage(container);

        assertEquals("Howdy", unifiedPushMessage.getMessage().getAlert());
        assertEquals(-1, unifiedPushMessage.getMessage().getBadge());
        assertFalse(unifiedPushMessage.getMessage().getApns().isContentAvailable());
    }

    @Test
    public void testAliasCriteria() throws IOException {
        final Map<String, Object> container = new LinkedHashMap<>();
        final Map<String, Object> messageObject = new LinkedHashMap<>();

        messageObject.put("alert", "Howdy");
        messageObject.put("sound", "default");
        messageObject.put("badge", 2);
        Map<String, String> data = new HashMap<>();
        data.put("someKey", "someValue");
        messageObject.put("user-data", data);

        container.put("message", messageObject);
        messageObject.put("simple-push", "version=123");

        // criteria:
        Map<String, Object> criteria = new HashMap<>();
        criteria.put("alias", Arrays.asList("foo@bar.org"));
        container.put("criteria", criteria);

        // parse it:
        final UnifiedPushMessage unifiedPushMessage = parsePushMessage(container);
        assertNotNull(unifiedPushMessage.getCriteria().getAliases());
        assertEquals(1, unifiedPushMessage.getCriteria().getAliases().size());
        assertEquals("foo@bar.org", unifiedPushMessage.getCriteria().getAliases().get(0));
    }

    @Test
    public void testAction() throws IOException{
        final Map<String, Object> container = new LinkedHashMap<>();
        final Map<String, Object> messageObject = new LinkedHashMap<>();
        final Map<String, Object> apnsObject = new LinkedHashMap<>();

        apnsObject.put("action", "View");

        messageObject.put("alert", "howdy");
        messageObject.put("apns",apnsObject);
        container.put("message", messageObject);

        // parse it:
        final UnifiedPushMessage unifiedPushMessage = parsePushMessage(container);
        assertEquals("View", unifiedPushMessage.getMessage().getApns().getAction());

    }

    @Test
    public void testUrlArgs() throws IOException {
        final Map<String, Object> container = new LinkedHashMap<>();
        final Map<String, Object> messageObject = new LinkedHashMap<>();
        final Map<String, Object> apnsObject = new LinkedHashMap<>();
        final String[] urlArgs = { "Arg1", "Arg2" };

        apnsObject.put("title", "I'm a Title");
        apnsObject.put("url-args", urlArgs);
        messageObject.put("apns",apnsObject);
        messageObject.put("alert", "howdy");

        container.put("message", messageObject);

        // parse it:
        final UnifiedPushMessage unifiedPushMessage = parsePushMessage(container);
        assertEquals("[Arg1, Arg2]", Arrays.toString(unifiedPushMessage.getMessage().getApns().getUrlArgs()));
    }

    @Test
    public void testActionCategory() throws IOException {
        final Map<String, Object> container = new LinkedHashMap<>();
        final Map<String, Object> messageObject = new LinkedHashMap<>();
        final Map<String, Object> apnsObject = new LinkedHashMap<>();

        apnsObject.put("action-category", "POSTS");
        messageObject.put("alert", "Howdy");
        messageObject.put("apns",apnsObject);
        container.put("message", messageObject);

        // parse it:
        final UnifiedPushMessage unifiedPushMessage = parsePushMessage(container);
        assertEquals("POSTS", unifiedPushMessage.getMessage().getApns().getActionCategory());
    }

    @Test
    public void testLocalizedKey() throws IOException {
        final Map<String, Object> container = new LinkedHashMap<>();
        final Map<String, Object> messageObject = new LinkedHashMap<>();
        final Map<String, Object> apnsObject = new LinkedHashMap<>();

        apnsObject.put("localized-key", "myLocalizedKey");
        messageObject.put("alert", "Howdy");
        messageObject.put("apns",apnsObject);
        container.put("message", messageObject);

        // parse it:
        final UnifiedPushMessage unifiedPushMessage = parsePushMessage(container);
        assertEquals("myLocalizedKey", unifiedPushMessage.getMessage().getApns().getLocalizedKey());
    }

    @Test
    public void testLocalizedArguments() throws IOException {
        final Map<String, Object> container = new LinkedHashMap<>();
        final Map<String, Object> messageObject = new LinkedHashMap<>();
        final Map<String, Object> apnsObject = new LinkedHashMap<>();
        String[] arguments = {"Jambon","ham"};
        apnsObject.put("localized-arguments", arguments);
        messageObject.put("alert", "Howdy");
        messageObject.put("apns",apnsObject);
        container.put("message", messageObject);

        // parse it:
        final UnifiedPushMessage unifiedPushMessage = parsePushMessage(container);
        assertEquals("[Jambon, ham]", Arrays.toString(unifiedPushMessage.getMessage().getApns().getLocalizedArguments()));
    }
	
    @Test
    public void testLocalizedTitleKey() throws IOException {
        final Map<String, Object> container = new LinkedHashMap<>();
        final Map<String, Object> messageObject = new LinkedHashMap<>();
        final Map<String, Object> apnsObject = new LinkedHashMap<>();

        apnsObject.put("localized-title-key", "myLocalizedTitle");
        messageObject.put("alert", "Howdy");
        messageObject.put("apns",apnsObject);
        container.put("message", messageObject);

        // parse it:
        final UnifiedPushMessage unifiedPushMessage = parsePushMessage(container);
        assertEquals("myLocalizedTitle", unifiedPushMessage.getMessage().getApns().getLocalizedTitleKey());
    }

    @Test
    public void testLocalizedTitleArguments() throws IOException {
        final Map<String, Object> container = new LinkedHashMap<>();
        final Map<String, Object> messageObject = new LinkedHashMap<>();
        final Map<String, Object> apnsObject = new LinkedHashMap<>();
        String[] arguments = {"Jambon","ham"};
        apnsObject.put("localized-title-arguments", arguments);
        messageObject.put("alert", "Howdy");
        messageObject.put("apns",apnsObject);
        container.put("message", messageObject);

        // parse it:
        final UnifiedPushMessage unifiedPushMessage = parsePushMessage(container);
        assertEquals("[Jambon, ham]", Arrays.toString(unifiedPushMessage.getMessage().getApns().getLocalizedTitleArguments()));
    }

    @Test
    public void testMultipleAliasCriteria() throws IOException {
        final Map<String, Object> container = new LinkedHashMap<>();
        final Map<String, Object> messageObject = new LinkedHashMap<>();

        messageObject.put("alert", "Howdy");
        messageObject.put("sound", "default");
        messageObject.put("badge", 2);
        Map<String, String> data = new HashMap<>();
        data.put("someKey", "someValue");
        messageObject.put("user-data", data);

        container.put("message", messageObject);
        messageObject.put("simple-push", "version=123");

        // criteria:
        Map<String, Object> criteria = new HashMap<>();
        criteria.put("alias", Arrays.asList("foo@bar.org", "bar@foo.com"));
        container.put("criteria", criteria);

        // parse it:
        final UnifiedPushMessage unifiedPushMessage = parsePushMessage(container);
        assertNotNull(unifiedPushMessage.getCriteria().getAliases());
        assertEquals(2, unifiedPushMessage.getCriteria().getAliases().size());
        assertTrue(unifiedPushMessage.getCriteria().getAliases().contains("foo@bar.org"));
        assertTrue(unifiedPushMessage.getCriteria().getAliases().contains("bar@foo.com"));
    }

    @Test
    public void testDeviceTypeCriteria() throws IOException {
        final Map<String, Object> container = new LinkedHashMap<>();
        final Map<String, Object> messageObject = new LinkedHashMap<>();

        messageObject.put("alert", "Howdy");
        messageObject.put("sound", "default");
        messageObject.put("badge", 2);
        Map<String, String> data = new HashMap<>();
        data.put("someKey", "someValue");
        messageObject.put("user-data", data);

        container.put("message", messageObject);
        messageObject.put("simple-push", "version=123");

        // criteria:
        Map<String, Object> criteria = new HashMap<>();
        criteria.put("deviceType", Arrays.asList("iPad"));
        container.put("criteria", criteria);

        // parse it:
        final UnifiedPushMessage unifiedPushMessage = parsePushMessage(container);
        assertNotNull(unifiedPushMessage.getCriteria().getDeviceTypes());
        assertEquals(1, unifiedPushMessage.getCriteria().getDeviceTypes().size());
        assertEquals("iPad", unifiedPushMessage.getCriteria().getDeviceTypes().get(0));
    }

    @Test
    public void testDeviceTypesCriteria() throws IOException {
        final Map<String, Object> container = new LinkedHashMap<>();
        final Map<String, Object> messageObject = new LinkedHashMap<>();

        messageObject.put("alert", "Howdy");
        messageObject.put("sound", "default");
        messageObject.put("badge", 2);

        container.put("message", messageObject);
        messageObject.put("simple-push", "version=123");

        // criteria:
        Map<String, Object> criteria = new HashMap<>();
        criteria.put("deviceType", Arrays.asList("iPad", "Android"));
        container.put("criteria", criteria);

        // parse it:
        final UnifiedPushMessage unifiedPushMessage = parsePushMessage(container);
        assertNotNull(unifiedPushMessage.getCriteria().getDeviceTypes());
        assertEquals(2, unifiedPushMessage.getCriteria().getDeviceTypes().size());
        assertTrue(unifiedPushMessage.getCriteria().getDeviceTypes().contains("iPad"));
        assertTrue(unifiedPushMessage.getCriteria().getDeviceTypes().contains("Android"));
    }

    @Test
    public void testCategoriesCriteria() throws IOException {
        final Map<String, Object> container = new LinkedHashMap<>();
        final Map<String, Object> messageObject = new LinkedHashMap<>();

        messageObject.put("alert", "Howdy");
        messageObject.put("sound", "default");
        messageObject.put("badge", 2);

        container.put("message", messageObject);

        // criteria:
        Map<String, Object> criteria = new HashMap<>();
        criteria.put("categories", Arrays.asList("football"));
        container.put("criteria", criteria);

        // parse it:
        final UnifiedPushMessage unifiedPushMessage = parsePushMessage(container);
        assertNotNull(unifiedPushMessage.getCriteria().getCategories());
        assertEquals(1, unifiedPushMessage.getCriteria().getCategories().size());
        assertEquals("football", unifiedPushMessage.getCriteria().getCategories().get(0));
    }

    @Test
    public void testMultipleCategoriesCriteria() throws IOException {
        final Map<String, Object> container = new LinkedHashMap<>();
        final Map<String, Object> messageObject = new LinkedHashMap<>();

        messageObject.put("alert", "Howdy");
        messageObject.put("sound", "default");
        messageObject.put("badge", 2);

        container.put("message", messageObject);

        // criteria:
        Map<String, Object> criteria = new HashMap<>();
        criteria.put("categories", Arrays.asList("soccer", "olympics"));
        container.put("criteria", criteria);

        // parse it:
        final UnifiedPushMessage unifiedPushMessage = parsePushMessage(container);
        assertNotNull(unifiedPushMessage.getCriteria().getCategories());
        assertEquals(2, unifiedPushMessage.getCriteria().getCategories().size());
        assertTrue(unifiedPushMessage.getCriteria().getCategories().contains("olympics"));
        assertTrue(unifiedPushMessage.getCriteria().getCategories().contains("soccer"));
    }

    @Test
    public void testVariantsCriteria() throws IOException {
        final Map<String, Object> container = new LinkedHashMap<>();
        final Map<String, Object> messageObject = new LinkedHashMap<>();

        messageObject.put("alert", "Howdy");
        messageObject.put("sound", "default");
        messageObject.put("badge", 2);

        container.put("message", messageObject);

        // criteria:
        Map<String, Object> criteria = new HashMap<>();
        criteria.put("variants", Arrays.asList("abc-123-def-456"));
        container.put("criteria", criteria);

        // parse it:
        final UnifiedPushMessage unifiedPushMessage = parsePushMessage(container);
        assertNotNull(unifiedPushMessage.getCriteria().getVariants());
        assertEquals(1, unifiedPushMessage.getCriteria().getVariants().size());
        assertEquals("abc-123-def-456", unifiedPushMessage.getCriteria().getVariants().get(0));
    }

    @Test
    public void testMultipleVariantsCriteria() throws IOException {
        final Map<String, Object> container = new LinkedHashMap<>();
        final Map<String, Object> messageObject = new LinkedHashMap<>();

        messageObject.put("alert", "Howdy");
        messageObject.put("sound", "default");
        messageObject.put("badge", 2);

        container.put("message", messageObject);

        // criteria:
        Map<String, Object> criteria = new HashMap<>();
        criteria.put("variants", Arrays.asList("abc-123-def-456", "456-abc-123-def-bar"));
        container.put("criteria", criteria);

        // parse it:
        final UnifiedPushMessage unifiedPushMessage = parsePushMessage(container);
        assertNotNull(unifiedPushMessage.getCriteria().getVariants());
        assertEquals(2, unifiedPushMessage.getCriteria().getVariants().size());
        assertTrue(unifiedPushMessage.getCriteria().getVariants().contains("abc-123-def-456"));
        assertTrue(unifiedPushMessage.getCriteria().getVariants().contains("456-abc-123-def-bar"));
    }

    @Test
    public void testAllCriteria() throws IOException {
        final Map<String, Object> container = new LinkedHashMap<>();
        final Map<String, Object> messageObject = new LinkedHashMap<>();

        messageObject.put("alert", "Howdy");
        messageObject.put("sound", "default");
        messageObject.put("badge", 2);
        Map<String, String> data = new HashMap<>();
        data.put("someKey", "someValue");
        messageObject.put("user-data", data);

        container.put("message", messageObject);
        messageObject.put("simple-push", "version=123");

        // criteria:
        Map<String, Object> criteria = new HashMap<>();
        criteria.put("variants", Arrays.asList("abc-123-def-456", "456-abc-123-def-bar"));
        criteria.put("categories", Arrays.asList("soccer", "olympics"));
        criteria.put("deviceType", Arrays.asList("iPad", "Android"));
        criteria.put("alias", Arrays.asList("foo@bar.org", "bar@foo.com"));
        container.put("criteria", criteria);

        // parse it:
        final UnifiedPushMessage unifiedPushMessage = parsePushMessage(container);

        assertEquals(2, unifiedPushMessage.getCriteria().getAliases().size());
        assertTrue(unifiedPushMessage.getCriteria().getAliases().contains("foo@bar.org"));
        assertFalse(unifiedPushMessage.getCriteria().getAliases().contains("mrx@bar.org"));

        assertEquals(2, unifiedPushMessage.getCriteria().getDeviceTypes().size());
        assertTrue(unifiedPushMessage.getCriteria().getDeviceTypes().contains("Android"));
        assertFalse(unifiedPushMessage.getCriteria().getDeviceTypes().contains("iPhone"));

        assertEquals(2, unifiedPushMessage.getCriteria().getCategories().size());
        assertTrue(unifiedPushMessage.getCriteria().getCategories().contains("olympics"));
        assertFalse(unifiedPushMessage.getCriteria().getCategories().contains("Bundesliga"));

        assertEquals(2, unifiedPushMessage.getCriteria().getVariants().size());
        assertTrue(unifiedPushMessage.getCriteria().getVariants().contains("abc-123-def-456"));
        assertFalse(unifiedPushMessage.getCriteria().getVariants().contains("0815"));
        assertEquals("version=123", unifiedPushMessage.getMessage().getSimplePush());
    }

    @Test(expected = JsonMappingException.class)
    public void testVariantCriteriaParseError() throws IOException {
        final Map<String, Object> container = new LinkedHashMap<>();
        Map<String, Object> criteria = new HashMap<>();
        criteria.put("variants", "abc-123-def-456");
        container.put("criteria", criteria);
        parsePushMessage(container);
    }

    @Test
    public void testMessageToStrippedJson() throws IOException, URISyntaxException {
        //given
        final Map<String, Object> container = new LinkedHashMap<>();
        final Map<String, Object> messageObject = new LinkedHashMap<>();
        final Map<String, Object> apnsObject = new LinkedHashMap<>();

        messageObject.put("alert", "HELLO!");
        messageObject.put("sound", "default");
        messageObject.put("badge", 2);
        Map<String, Object> data = new HashMap<>();
        data.put("key", "value");
        data.put("key2", "value");
        messageObject.put("user-data", data);
        apnsObject.put("action-category", "category");
        apnsObject.put("content-available", "true");
        messageObject.put("simple-push", "version=123");
        messageObject.put("apns",apnsObject);

        container.put("message", messageObject);

        //when
        final UnifiedPushMessage unifiedPushMessage = parsePushMessage(container);
        String json = unifiedPushMessage.toStrippedJsonString();

        //then
        Path path = Paths.get(getClass().getResource("/message-tostrippedjson.json").toURI());
        String expectedJson = new String(Files.readAllBytes(path), Charset.defaultCharset());
        //poor mans equals ignore white space
        assertEquals(expectedJson.replaceAll("\\s", ""), json);
    }

    @Test
    public void testMessageToJson() throws IOException, URISyntaxException {
        //given
        final Map<String, Object> container = new LinkedHashMap<>();
        final Map<String, Object> messageObject = new LinkedHashMap<>();
        final Map<String, Object> apnsObject = new LinkedHashMap<>();

        messageObject.put("alert", "HELLO!");
        messageObject.put("sound", "default");
        messageObject.put("badge", 2);
        Map<String, Object> data = new HashMap<>();
        data.put("key", "value");
        data.put("key2", "value");
        messageObject.put("user-data", data);
        apnsObject.put("action-category", "category");
        apnsObject.put("content-available", "true");
        messageObject.put("simple-push", "version=123");
        messageObject.put("apns",apnsObject);

        container.put("message", messageObject);

        //when
        final UnifiedPushMessage unifiedPushMessage = parsePushMessage(container);
        String json = unifiedPushMessage.toStrippedJsonString();

        //then
        Path path = Paths.get(getClass().getResource("/message-tojson.json").toURI());
        String expectedJson = new String(Files.readAllBytes(path), Charset.defaultCharset());
        //poor mans equals ignore white space
        assertEquals(expectedJson.replaceAll("\\s", ""), json);
    }

    private UnifiedPushMessage parsePushMessage(Map<String, Object> container) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        final String valueAsString = mapper.writeValueAsString(container);

        return mapper.readValue(valueAsString, UnifiedPushMessage.class);
    }
}
