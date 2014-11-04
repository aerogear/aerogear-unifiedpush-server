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

/**
 * The APNs variant class encapsulates APNs specific behavior.
 */
public class APNsVariantTest {

    private APNsVariant variant;

    @Before
    public void setup() {
        variant = new APNsVariant();
        variant.setDescription("desc");
        variant.setDeveloper("Admin");
        variant.setName("APNs Variant");
        variant.setCertificate("someCert".getBytes());
        variant.setPassphrase("passphrase");
    }

    @Test
    public void iOSValues() {

        assertThat(variant).isNotNull();

        assertThat(variant.getVariantID()).isNotNull();
        assertThat(variant.getSecret()).isNotNull();

        assertThat(variant.getDescription()).isEqualTo("desc");
        assertThat(variant.getDeveloper()).isEqualTo("Admin");
        assertThat(variant.getName()).isEqualTo("APNs Variant");

        assertThat(variant.getType()).isEqualTo(VariantType.APNS);
        assertThat(variant.getCertificate()).isEqualTo("someCert".getBytes());
        assertThat(variant.getPassphrase()).isEqualTo("passphrase");
        assertThat(variant.isProduction()).isFalse();

        variant.setProduction(Boolean.TRUE);
        assertThat(variant.isProduction()).isTrue();
    }
}
