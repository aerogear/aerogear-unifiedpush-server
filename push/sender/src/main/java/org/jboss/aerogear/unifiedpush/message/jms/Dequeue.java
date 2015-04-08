package org.jboss.aerogear.unifiedpush.message.jms;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.inject.Qualifier;

/**
 * Denotes a message that is pulled from JMS queue for further processing
 *
 * @author Lukas Fryc
 */
@Qualifier
@Target({ ElementType.PARAMETER, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface Dequeue {
}