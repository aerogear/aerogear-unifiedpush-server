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

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.util.Arrays;
import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;

public class InstallationTest {

    private Installation deviceInstallation;

    @Before
    public void setup() {
        deviceInstallation = new Installation();

        deviceInstallation.setDeviceType("iPhone");
        deviceInstallation.setAlias("matzew");
        deviceInstallation.setCategories(new HashSet<String>(Arrays.asList("sports", "soccer")));
        deviceInstallation.setDeviceToken("1456782");
        deviceInstallation.setOperatingSystem("iOS");
        deviceInstallation.setOsVersion("7.0.6");
        deviceInstallation.setPlatform("iOS");
    }

    @Test
    public void installationValues() {
        assertThat(deviceInstallation.getDeviceType()).isEqualTo("iPhone");
        assertThat(deviceInstallation.getAlias()).isEqualTo("matzew");
        assertThat(deviceInstallation.getCategories()).contains("sports");
        assertThat(deviceInstallation.getCategories()).contains("soccer");
        assertThat(deviceInstallation.getDeviceToken()).isEqualTo("1456782");
        assertThat(deviceInstallation.getOperatingSystem()).isEqualTo("iOS");
        assertThat(deviceInstallation.getOsVersion()).isEqualTo("7.0.6");
        assertThat(deviceInstallation.getPlatform()).isEqualTo("iOS");
    }

    @Test
    public void disable() {
        assertThat(deviceInstallation.isEnabled()).isTrue();

        deviceInstallation.setEnabled(Boolean.FALSE);
        assertThat(deviceInstallation.isEnabled()).isFalse();
    }

    @Test
    public void simplePushEndpoint() {
        deviceInstallation.setSimplePushEndpoint("http://server.com/update/21345321354");
        assertThat(deviceInstallation.getSimplePushEndpoint()).isEqualTo("http://server.com/update/21345321354");
    }

    @Test
    public void shouldSerialize() throws JAXBException {
        JAXBContext jc = JAXBContext.newInstance(AndroidVariant.class);

        Marshaller marshaller = jc.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

        AndroidVariant variant = new AndroidVariant();
        variant.setName("serialize me");
        final Installation installation = new Installation();
        installation.setAlias("ignore me please");
        variant.getInstallations().add(installation);
        marshaller.marshal(variant, System.out);
    }
}
