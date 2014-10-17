package org.jboss.aerogear.unifiedpush.service.annotations;

import javax.inject.Qualifier;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Refer to the search service for admins or developers
 */
@Qualifier
@Retention( RetentionPolicy.RUNTIME )
@Target( {ElementType.FIELD, ElementType.METHOD})
public @interface SearchService {
}
