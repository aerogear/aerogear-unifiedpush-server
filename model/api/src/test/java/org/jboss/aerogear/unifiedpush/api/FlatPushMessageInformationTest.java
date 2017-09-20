/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
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
package org.jboss.aerogear.unifiedpush.api;

import org.junit.Before;
import org.junit.Test;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

public class FlatPushMessageInformationTest {

    private FlatPushMessageInformation pushMessageInformation;
    private Date openAppDate = new Date();
    private Date lastOpenDate = new Date();

    @Before
    public void setup() {


        // general job data
        pushMessageInformation = new FlatPushMessageInformation();
        pushMessageInformation.setId("123");
        pushMessageInformation.setPushApplicationId("123");
        pushMessageInformation.setRawJsonMessage("{\"data\" : \"something\"}");
        pushMessageInformation.setIpAddress("127.0.0.1");
        pushMessageInformation.setClientIdentifier("Java Sender Client");
        pushMessageInformation.setAppOpenCounter(Long.valueOf(1));
        pushMessageInformation.setFirstOpenDate(openAppDate);
        pushMessageInformation.setLastOpenDate(lastOpenDate);


        Variant variant1 = new AndroidVariant();
        variant1.setId("345");
        variant1.setVariantID("345");
        Variant variant2 = new AndroidVariant();
        variant2.setId("678");
        variant2.setVariantID("678");

        // two involved variants:
        VariantErrorStatus variantInfo1 = new VariantErrorStatus(pushMessageInformation, variant1, "Some error");
        VariantErrorStatus variantInfo2 = new VariantErrorStatus(pushMessageInformation, variant2, "Some other failure");

        // add the variant metadata:
        pushMessageInformation.getErrors().add(variantInfo1);
        pushMessageInformation.getErrors().add(variantInfo2);
    }

    @Test
    public void checkPushMessageInformation() {

        assertThat(pushMessageInformation.getErrors()).hasSize(2);
        assertThat(pushMessageInformation.getErrors()).extracting("variantID", "errorReason")
                .contains(
                        tuple("345", "Some error"),
                        tuple("678", "Some other failure")
                );

        assertThat(pushMessageInformation.getRawJsonMessage()).isEqualTo("{\"data\" : \"something\"}");
        assertThat(pushMessageInformation.getSubmitDate()).isNotNull();
        assertThat(pushMessageInformation.getId()).isNotNull();
        assertThat(pushMessageInformation.getIpAddress()).isEqualTo("127.0.0.1");
        assertThat(pushMessageInformation.getPushApplicationId()).isEqualTo("123");
        assertThat(pushMessageInformation.getClientIdentifier()).isEqualTo("Java Sender Client");
        assertThat(pushMessageInformation.getAppOpenCounter()).isEqualTo(1);
        assertThat(pushMessageInformation.getFirstOpenDate()).isEqualTo(openAppDate);
        assertThat(pushMessageInformation.getLastOpenDate()).isEqualTo(lastOpenDate);
    }
}
