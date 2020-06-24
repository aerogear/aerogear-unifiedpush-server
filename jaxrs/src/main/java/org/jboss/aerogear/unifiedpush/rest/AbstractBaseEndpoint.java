/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.aerogear.unifiedpush.rest;

import org.jboss.aerogear.unifiedpush.rest.util.error.ErrorBuilder;
import org.jboss.aerogear.unifiedpush.service.PushSearchService;
import org.jboss.aerogear.unifiedpush.service.impl.SearchManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Base class for all RESTful endpoints. Offers hooks for common features like validation
 */
public abstract class AbstractBaseEndpoint {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Inject
    private Validator validator;

    @Inject
    private SearchManager searchManager;

    @Context
    private HttpServletRequest httpServletRequest;

    // required for RESTEasy
    public AbstractBaseEndpoint() {
    }

    // required for tests
    protected AbstractBaseEndpoint(Validator validator, SearchManager searchManager) {
        this.validator = validator;
        this.searchManager = searchManager;
    }

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
                .entity(ErrorBuilder.forValidation().setMap(responseObj).build());
    }

    /**
     * offers PushSearchService to subclasses
     *
     * @return the push search service
     */
    protected PushSearchService getSearch() {
        return searchManager.getSearchService();
    }

}
