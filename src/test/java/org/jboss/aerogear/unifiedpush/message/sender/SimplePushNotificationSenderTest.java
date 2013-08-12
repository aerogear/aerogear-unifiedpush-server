/**
 * JBoss, Home of Professional Open Source Copyright Red Hat, Inc., and individual contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package org.jboss.aerogear.unifiedpush.message.sender;

import static org.junit.Assert.*;

import java.net.HttpURLConnection;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

public class SimplePushNotificationSenderTest {

    @BeforeClass
    public static void setDebug() {
        //System.setProperty("javax.net.debug", "all");
    }

    @Test
    @Ignore("This is intended to be a manual test just to verify that https works in combination with OpenShift")
    public void sendWithHttpsOpenShift() throws Exception {
        final String url = "https://tls-dbevenius.rhcloud.com:8443/update/124555";
        final HttpURLConnection connection = new SimplePushNotificationSender().put(url, "version=1");
        assertEquals(200, connection.getResponseCode());
    }

    @Test
    @Ignore("This is intended to be a manual test just to verify that https works in combination with OpenShift")
    public void sendWithHttpsMozilla() throws Exception {
        final String url = " https://push.services.mozilla.com/update/LZTeLaHHPFNWb3JJElXccTG_vacmdwgjdjbgQ-LupyH4HOFWELOMDkiHpQu6xfykZaQ8A6TpDfojYDWYAfwnUJIHCTmKbnS8Ql6GBs6LlZXkBofnKA==";
        final HttpURLConnection connection = new SimplePushNotificationSender().put(url, "version=1");
        assertEquals(200, connection.getResponseCode());
    }

}
