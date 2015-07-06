package org.jboss.aerogear.unifiedpush.message.jms.util;

import javax.jms.JMSException;
import javax.jms.Session;

public interface JMSOperation<T> {
    T execute(Session session) throws JMSException;
}
