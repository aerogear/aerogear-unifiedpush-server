/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.unifiedpush.message.sender.fcm;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import org.jboss.aerogear.unifiedpush.system.ConfigurationUtils;

import com.google.android.gcm.server.Sender;

public class ConfigurableFCMSender extends Sender {
	
	public static final String CUSTOM_AEROGEAR_FCM_PUSH_HOST = "custom.aerogear.fcm.push.host";
	
	public static final String FCM_ENDPOINT_HOST = "https://fcm.googleapis.com/fcm/send";

	public ConfigurableFCMSender(String key) {
		super(key);
	}

	@Override
	protected HttpURLConnection getConnection(String url) throws IOException {

		// let's see if there is a different URL we should post to (e.g. load/stress testing)
		final String fcmURL = ConfigurationUtils.tryGetProperty(CUSTOM_AEROGEAR_FCM_PUSH_HOST, FCM_ENDPOINT_HOST);

		return (HttpURLConnection) new URL(fcmURL).openConnection();
	}

	@Override
	protected HttpURLConnection post(String url, String contentType, String body) throws IOException {
		return super.post(FCM_ENDPOINT_HOST, contentType, body);
	}
}
