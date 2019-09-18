/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
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
package org.jboss.aerogear.unifiedpush.rest.util;

import org.jboss.aerogear.unifiedpush.api.WebPushVariant;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Locale;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class WebPushVariantTest {

    static Validator validator;

    @BeforeClass
    public static void before() throws Exception {
        Locale.setDefault(Locale.ENGLISH);
        ValidatorFactory config = Validation.buildDefaultValidatorFactory();
        validator = config.getValidator();
    }

    @Test
    public void testIsKeypairNotValid() throws Exception {
        //given
        WebPushVariant form = new WebPushVariant();
        form.setName("webPush");
        form.setAlias("https://redhat.com");
        form.setPrivateKey("gibberishKey");
        form.setPublicKey("BIk8YK3iWC3BfMt3GLEghzY4v5GwaZsTWKxDKm-FZry3Nx2E_q-4VW3501DkQ5TX1Pe7c3yIsajUk9hQAo3sT-0");
        //when
        final Set<ConstraintViolation<WebPushVariant>> constraintViolations = validator.validate(form);


        //then
        assertThat(constraintViolations).hasSize(1);
        assertThat(constraintViolations.iterator().next().getMessage())
                .isEqualTo("the provided private key does not match with the public key");
    }

    @Test
    public void testIsCertificatePassPhraseValid() throws Exception {
        //given
        WebPushVariant form = new WebPushVariant();
        form.setName("webPush");
        form.setAlias("https://redhat.com");
        form.setPrivateKey("FTg6q0-BXP6m-i6cNpg8P6JKccCUwWaD4yuirotxqXo");
        form.setPublicKey("BIk8YK3iWC3BfMt3GLEghzY4v5GwaZsTWKxDKm-FZry3Nx2E_q-4VW3501DkQ5TX1Pe7c3yIsajUk9hQAo3sT-0");
        //when
        final Set<ConstraintViolation<WebPushVariant>> constraintViolations = validator.validate(form);

        //then
        assertThat(constraintViolations).isEmpty();
    }
}