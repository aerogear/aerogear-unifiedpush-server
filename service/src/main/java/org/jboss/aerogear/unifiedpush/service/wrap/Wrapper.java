package org.jboss.aerogear.unifiedpush.service.wrap;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.inject.Qualifier;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.PARAMETER, ElementType.FIELD, ElementType.TYPE, ElementType.METHOD })
@Qualifier
public @interface Wrapper {

}
