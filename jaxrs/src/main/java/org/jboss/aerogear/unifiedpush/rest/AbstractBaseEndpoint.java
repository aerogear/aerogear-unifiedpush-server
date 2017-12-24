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

package org.jboss.aerogear.unifiedpush.rest;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.jboss.aerogear.unifiedpush.service.annotations.LoggedInUser;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Base class for all RESTful endpoints. Offers hooks for common features like validation
 */
public abstract class AbstractBaseEndpoint extends AbstractEndpoint {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Inject
    private Validator validator;

    @Inject
    private MessageSource messageSource;

    /**
     * Generic validator used to identify constraint violations of the given model class.
     *
     * @param model object to validate
     * @throws ConstraintViolationException if constraint violations on the given model have been identified.
     */
    protected void validateModelClass(Object model) {
        final Set<ConstraintViolation<Object>> violations = validator.validate(model);

        // in case of an invalid model, we throw a ConstraintViolationException, containing the violations:
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(
                    new HashSet<>(violations));
        }
    }

    /**
     * Helper function to create a 400 Bad Request response, containing a JSON map giving details about the violations
     *
     * @param violations set of occurred constraint violations
     * @return 400 Bad Request response, containing details on the constraint violations
     */
    protected ResponseBuilder createBadRequestResponse(Set<ConstraintViolation<?>> violations) {
        final Map<String, String> responseObj = violations.stream()
                .collect(Collectors.toMap(v -> v.getPropertyPath().toString(), ConstraintViolation::getMessage));

        return Response.status(Response.Status.BAD_REQUEST)
                           .entity(responseObj);
    }

    /**
     * Helper function to create a 400 Bad Request response, containing a JSON map giving details about the exception
     *
     * @param e thrown exception
     * @param source source validation entity
     * @param suffix validation code suffix
     * @param args message source args
     * @return 400 Bad Request response, containing details on the constraint violations
     */
	protected ResponseBuilder createBadRequestResponse(Exception e, Class<?> source, String suffix, String... args) {
		final Map<String, String> responseObj = new HashMap<>();

		String code = source.getName().toLowerCase() + "." + suffix;
		String message = messageSource.getMessage(code, args, Locale.getDefault());

		responseObj.put(code, message);
		return Response.status(Response.Status.BAD_REQUEST).entity(responseObj);
	}

    /**
     * Helper function to create a 400 Bad Request response, containing a JSON map giving details about the exception
     *
     * @param e thrown exception
     * @param source source validation entity
     * @param suffix validation code suffix
     * @param args message source args
     * @return 409 Bad Request response, containing details on the constraint violations
     */
	protected ResponseBuilder createConflictRequestResponse(Exception e, Class<?> source, String suffix, String... args) {
		final Map<String, String> responseObj = new HashMap<>();

		String code = source.getName().toLowerCase() + "." + suffix;
		String message = messageSource.getMessage(code, args, Locale.getDefault());

		responseObj.put(code, message);
		return Response.status(Response.Status.CONFLICT).entity(responseObj);
	}

	/**
	 * Extract the username to be used in multiple queries
	 *
	 * @return current logged in user
	 */
    public static LoggedInUser extractUsername() {
        KeycloakAuthenticationToken token = ((KeycloakAuthenticationToken) SecurityContextHolder.getContext().getAuthentication());

		// Null check for automation scenarios.
		if (token != null && token.getPrincipal() != null) {
			KeycloakPrincipal<?> p = (KeycloakPrincipal<?>) token.getPrincipal();

			KeycloakSecurityContext kcSecurityContext = p.getKeycloakSecurityContext();
			return new LoggedInUser(kcSecurityContext.getToken().getPreferredUsername());
		}


		return new LoggedInUser("NULL");
    }

}
