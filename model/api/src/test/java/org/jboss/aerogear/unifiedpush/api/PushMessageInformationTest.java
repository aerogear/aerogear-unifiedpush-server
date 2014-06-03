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


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

public class PushMessageInformationTest {

    private PushMessageInformation pushMessageInformation;

    @Before
    public void setup() {
        // general job data
        pushMessageInformation = new PushMessageInformation();
        pushMessageInformation.setPushApplicationId("123");
        pushMessageInformation.setRawJsonMessage("{\"data\" : \"something\"}");
        pushMessageInformation.setIpAddress("127.0.0.1");
        pushMessageInformation.setClientIdentifier("Java Sender Client");

        // two involved variants:
        VariantMetricInformation variantInfo1 = new VariantMetricInformation();
        variantInfo1.setVariantID("345");
        variantInfo1.setReceivers(500);
        variantInfo1.setDeliveryStatus(Boolean.FALSE);

        VariantMetricInformation variantInfo2 = new VariantMetricInformation();
        variantInfo2.setVariantID("678");
        variantInfo2.setReceivers(100);
        variantInfo2.setDeliveryStatus(Boolean.TRUE);

        // add the variant metadata:
        pushMessageInformation.getVariantInformations().add(variantInfo1);
        pushMessageInformation.getVariantInformations().add(variantInfo2);
    }

    @Test
    public void checkPushMessageInformation() {

        assertThat(pushMessageInformation.getVariantInformations()).hasSize(2);
        assertThat(pushMessageInformation.getVariantInformations()).extracting("receivers", "deliveryStatus")
                .contains(
                        tuple(500L, Boolean.FALSE),
                        tuple(100L, Boolean.TRUE)
                );

        assertThat(pushMessageInformation.getRawJsonMessage()).isEqualTo("{\"data\" : \"something\"}");
        assertThat(pushMessageInformation.getSubmitDate()).isNotNull();
        assertThat(pushMessageInformation.getId()).isNotNull();
        assertThat(pushMessageInformation.getIpAddress()).isEqualTo("127.0.0.1");
        assertThat(pushMessageInformation.getPushApplicationId()).isEqualTo("123");
        assertThat(pushMessageInformation.getClientIdentifier()).isEqualTo("Java Sender Client");
    }
}
