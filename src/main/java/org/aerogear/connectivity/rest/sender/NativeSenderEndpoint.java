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

import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.inject.Inject;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.Topic;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import org.aerogear.connectivity.model.PushApplication;
import org.aerogear.connectivity.service.PushApplicationService;
import org.aerogear.connectivity.service.SenderService;


@Stateless
@Path("/sender")
@TransactionAttribute
public class NativeSenderEndpoint {
    @Inject
    private PushApplicationService pushApplicationService;
    @Inject
    private SenderService senderService;
    
//    @Resource(mappedName = "java:/ConnectionFactory")
//    private ConnectionFactory connectionFactory;
//    @Resource(mappedName = "java:/topic/aerogear/sender")
//    private Topic globalSenderTopic;
//    
//    Connection connection = null;
//    Session session = null;
    
    @POST
    @Path("/broadcast/{pushApplicationID}")
    @Consumes("application/json")
    public Response broadcast(LinkedHashMap<String, String> message, @PathParam("pushApplicationID") String pushApplicationID) {
        
        PushApplication pushApp = pushApplicationService.findPushApplicationById(pushApplicationID);
        senderService.broadcast(pushApp, message);
        
        
//        try {
//            connection = connectionFactory.createConnection();
//            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
//            MessageProducer messageProducer = session.createProducer(globalSenderTopic);
//            connection.start();
//            
//            ObjectMessage objMessage = session.createObjectMessage(message);
//            objMessage.setStringProperty("pushApplicationID", pushApplicationID);
//            
//            
//            messageProducer.send(objMessage);
//            
//            session.close();
//            connection.close();
//            
//        } catch (JMSException e) {
//            e.printStackTrace();
//        }

        return Response.status(200)
                .entity("Job submitted").build();
    }

}
