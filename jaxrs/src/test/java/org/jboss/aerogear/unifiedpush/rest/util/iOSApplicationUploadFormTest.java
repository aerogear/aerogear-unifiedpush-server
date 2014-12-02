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
package org.jboss.aerogear.unifiedpush.rest.util;

import org.apache.commons.io.IOUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Locale;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class iOSApplicationUploadFormTest {

    static Validator validator;

    @BeforeClass
    public static void before() throws Exception {
        Locale.setDefault(Locale.ENGLISH);
        ValidatorFactory config = Validation.buildDefaultValidatorFactory();
        validator = config.getValidator();
    }

    @Test
    public void testIsCertificatePassPhraseNotValid() throws Exception {
        //given
        iOSApplicationUploadForm form = new iOSApplicationUploadForm();

        //when
        final Set<ConstraintViolation<iOSApplicationUploadForm>> constraintViolations = validator.validate(form);


        //then
        assertThat(constraintViolations).hasSize(1);
        assertThat(constraintViolations.iterator().next().getMessage())
                .isEqualTo("the provided certificate passphrase does not match with the uploaded certificate");
    }

    @Test
    public void testIsCertificatePassPhraseValid() throws Exception {
        //given
        iOSApplicationUploadForm form = new iOSApplicationUploadForm();
        form.setCertificate(IOUtils.toByteArray(getClass().getResourceAsStream("/Certificates.p12")));
        form.setPassphrase("aero1gears");

        //when
        final Set<ConstraintViolation<iOSApplicationUploadForm>> constraintViolations = validator.validate(form);

        //then
        assertThat(constraintViolations).isEmpty();
    }
}