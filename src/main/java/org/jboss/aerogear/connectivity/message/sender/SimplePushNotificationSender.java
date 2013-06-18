/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors
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

package org.jboss.aerogear.connectivity.message.sender;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.aerogear.connectivity.message.sender.annotations.SimplePushSender;

import com.ning.http.client.AsyncHttpClient;
import java.io.Serializable;

@SimplePushSender
@ApplicationScoped
public class SimplePushNotificationSender implements Serializable {
    private static final long serialVersionUID = 5747687132270998712L;



    private AsyncHttpClient asyncHttpClient;
    @Inject private Logger logger;    
    
    @PostConstruct
    public void createAsyncHttpClient() {
        asyncHttpClient = new AsyncHttpClient();
    }
    
    public void sendMessage(String endpoint, String payload, List<String> channels) {
        // iterate over all the given channels:
        for (String channelID : channels) {

            try {
                // blocking IO here:
                com.ning.http.client.Response response =
                        asyncHttpClient.preparePut(endpoint + channelID)
                          .addHeader("Accept", "application/x-www-form-urlencoded")
                          .setBody(payload) // should be a string like 'version=123'
                          .execute().get();

                int simplePushStatusCode = response.getStatusCode();
                logger.info("SimplePush Status: " + simplePushStatusCode);
                if (200 != simplePushStatusCode) {
                    logger.severe("ERROR ??????     STATUS CODE, from PUSH NETWORK was NOT 200, but....: " + simplePushStatusCode);
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @PreDestroy
    public void closeAsyncHttpClient() {
        asyncHttpClient.close();
    }
}
