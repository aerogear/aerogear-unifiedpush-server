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
package org.jboss.aerogear.unifiedpush.rest.annotations;

import javax.enterprise.util.Nonbinding;
import javax.ws.rs.NameBinding;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This endpoint may be disabled by an environment variable, UPS_DISABLED.
 * This variable takes a comma separated list of endpoints to disable.
 * 
 * This is used by the variant endpoints to disable certain REST endpoints per 
 * environment.
 * 
 * To disable web push you would use 
 * <code>
 * export UPS_DISABLED=web_push,webpush
 * </code>
 *
 * To disable everything you would use 
 * <code>
 * export UPS_DISABLED=web_push,webpush,android,ios,ios_token,iostoken
 * </code>
 * 
 */
@NameBinding
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface DisabledByEnvironment {
    @Nonbinding String[] value() default "";
}
