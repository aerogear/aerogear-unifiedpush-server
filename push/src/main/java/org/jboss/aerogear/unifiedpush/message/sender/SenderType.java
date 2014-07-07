package org.jboss.aerogear.unifiedpush.message.sender;

import org.jboss.aerogear.unifiedpush.api.Variant;

import javax.inject.Qualifier;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 */
@Qualifier
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface SenderType {
    Class<? extends Variant> value();
}
