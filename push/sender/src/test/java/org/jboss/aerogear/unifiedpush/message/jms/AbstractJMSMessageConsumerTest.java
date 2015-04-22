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
package org.jboss.aerogear.unifiedpush.message.jms;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.jms.JMSException;
import javax.jms.ObjectMessage;
import javax.jms.Queue;

import org.junit.Before;
import org.junit.Test;

public class AbstractJMSMessageConsumerTest {

    private ObjectMessage jmsMessage;
    private Queue destination;

    @Before
    public void setup() throws JMSException {
        // given
        destination = mock(Queue.class);
        when(destination.getQueueName()).thenReturn("/queue/mock");

        jmsMessage = mock(ObjectMessage.class);
        when(jmsMessage.getJMSDestination()).thenReturn(destination);
    }

    @Test
    public void testCorrectPayload() throws JMSException {
        AtomicBoolean payload = new AtomicBoolean(Boolean.FALSE);
        when(jmsMessage.getObject()).thenReturn(payload);

        // when
        MockMessageConsumer consumer = new MockMessageConsumer();
        consumer.onMessage(jmsMessage);

        // then
        assertTrue("message was delivered", payload.get());
    }

    @Test(expected = IllegalStateException.class)
    public void testIncorrectTypeOfPayload() throws JMSException {
        when(jmsMessage.getObject()).thenReturn(Integer.MIN_VALUE);

        // when
        MockMessageConsumer consumer = new MockMessageConsumer();
        consumer.onMessage(jmsMessage);
    }

    public static class MockMessageConsumer extends AbstractJMSMessageConsumer<AtomicBoolean> {

        @Override
        public void onMessage(AtomicBoolean message) {
            message.set(Boolean.TRUE);
        }
    }

}
