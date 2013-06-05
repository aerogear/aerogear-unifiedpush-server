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

package org.aerogear.connectivity.rest.sender;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import org.aerogear.connectivity.model.MobileVariantInstanceImpl;
import org.aerogear.connectivity.model.SimplePushVariant;
import org.aerogear.connectivity.service.SimplePushVariantService;

import com.ning.http.client.AsyncHttpClient;

@Stateless
@Path("/sender/simplePush")
public class SimplePushSender {

    @Inject private Logger logger;
    
    @Inject
    private SimplePushVariantService simplePushApplicationService;
    private AsyncHttpClient asyncHttpClient;
    
    @PostConstruct
    public void createAsyncHttpClient() {
        asyncHttpClient = new AsyncHttpClient();
    }

    @POST
    @Path("/broadcast/{id}") //TODO: URL name sucks
    @Consumes("application/json")
    public Response broadcastSimplePush(Map message, @PathParam("id") String simplePushId) {
        
        logger.severe("Broadcast ID: " + simplePushId);

        SimplePushVariant spa = simplePushApplicationService.findByVariantID(simplePushId);
        String endpoint = spa.getPushNetworkURL();
        
        logger.severe("SimplePush NetworkURL: " + endpoint);
        
        String version = (String) message.get("version");
        
        logger.severe("submitted version: " + version);
        
        Set<MobileVariantInstanceImpl> instances = spa.getInstances();
        
        logger.severe("Number of instances (total):  " + instances.size());
        
        
        List<String> broadcastTokens = new ArrayList<String>();
        for (MobileVariantInstanceImpl mobileApplicationInstance : instances) {
            
            if ("broadcast".equalsIgnoreCase(mobileApplicationInstance.getCategory())) {
                
                broadcastTokens.add(mobileApplicationInstance.getDeviceToken());
            }
        }
        logger.severe("Number of instances (broadcast CATEGORY):  " + broadcastTokens.size());

        this.performHTTP(endpoint, version, broadcastTokens);
        
        return Response.status(200)
                .entity("Job submitted").build();
    }

    @POST
    @Path("/selected/{id}")
    @Consumes("application/json")
    public Response notifyGivenChannels(Map message, @PathParam("id") String simplePushId) {
        
        
        SimplePushVariant spa = simplePushApplicationService.findByVariantID(simplePushId);
        String endpoint = spa.getPushNetworkURL();
        
        String version = (String) message.get("version");
        List<String> channelIDList = (List<String>) message.get("channelIDs");
        
        this.performHTTP(endpoint, version, channelIDList);
        
        return Response.status(200)
                .entity("Job submitted").build();
    }
    
    
    
    private void performHTTP(String endpoint, String payload, List<String> tokenList) {
        final CountDownLatch latch = new CountDownLatch(tokenList.size());
        
        for (String token : tokenList) {

            try {
                com.ning.http.client.Response response =
                        asyncHttpClient.preparePut(endpoint + token)
                          .addHeader("Accept", "application/x-www-form-urlencoded")
                          .setBody("version=" + Integer.parseInt(payload))
                          .execute().get();
                
                latch.countDown();
                int simplePushStatusCode = response.getStatusCode();
                logger.severe("SimplePush Status: " + simplePushStatusCode);
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
        
        try {
            // all responses received ?
            latch.await();
            logger.severe("......after await....  all response should have been received, by now...");
        } catch (InterruptedException e) {
            Thread.interrupted();
        }
    }
    
    @PreDestroy
    public void closeAsyncHttpClient() {
        asyncHttpClient.close();
    }
    
}
