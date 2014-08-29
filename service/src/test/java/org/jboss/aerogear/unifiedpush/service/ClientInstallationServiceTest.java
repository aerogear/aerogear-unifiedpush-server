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
package org.jboss.aerogear.unifiedpush.service;

import org.apache.openejb.jee.Beans;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Module;
import org.jboss.aerogear.unifiedpush.api.AndroidVariant;
import org.jboss.aerogear.unifiedpush.api.Installation;
import org.jboss.aerogear.unifiedpush.jpa.dao.impl.JPAInstallationDao;
import org.jboss.aerogear.unifiedpush.jpa.dao.impl.JPAVariantDao;
import org.jboss.aerogear.unifiedpush.service.impl.ClientInstallationServiceImpl;
import org.jboss.aerogear.unifiedpush.service.impl.GenericVariantServiceImpl;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(ApplicationComposer.class)
public class ClientInstallationServiceTest extends AbstractBaseServiceTest {


    @Inject
    private ClientInstallationService clientInstallationService;
    @Inject
    private GenericVariantService variantService;

    @Module
    public Beans getBeans() {
        final Beans beans = new Beans();
        beans.addManagedClass(ClientInstallationServiceImpl.class);
        beans.addManagedClass(JPAInstallationDao.class);
        beans.addManagedClass(GenericVariantServiceImpl.class);
        beans.addManagedClass(JPAVariantDao.class);

        return beans;
    }

    @Test
    public void importDevicesWithoutDuplicates() {
        // setup a variant:
        AndroidVariant av = new AndroidVariant();
        av.setGoogleKey("Key");
        av.setName("Android");
        av.setDeveloper("me");
        variantService.addVariant(av);

        assertThat(variantService.findByVariantID(av.getVariantID())).isNotNull();

        // generate some devices:
        final int NUMBER_OF_INSTALLATIONS = 5;
        final List<Installation> devices = new ArrayList<Installation>();
        for (int i = 0; i < NUMBER_OF_INSTALLATIONS; i++) {
            Installation device = new Installation();
            device.setDeviceToken(generateFakedDeviceTokenString());
            devices.add(device);
        }

        clientInstallationService.addInstallations(av, devices);
        assertThat(clientInstallationService.findAllDeviceTokenForVariantIDByCriteria(av.getVariantID(), null, null, null)).hasSize(NUMBER_OF_INSTALLATIONS);

        // add just one device:
        Installation device = new Installation();
        device.setDeviceToken(generateFakedDeviceTokenString());
        devices.add(device);

        // run the importer again
        clientInstallationService.addInstallations(av, devices);
        assertThat(clientInstallationService.findAllDeviceTokenForVariantIDByCriteria(av.getVariantID(), null, null, null)).hasSize(NUMBER_OF_INSTALLATIONS + 1);
    }

    @Test
    public void importDevices() {
        // setup a variant:
        AndroidVariant av = new AndroidVariant();
        av.setGoogleKey("Key");
        av.setName("Android");
        av.setDeveloper("me");
        variantService.addVariant(av);

        assertThat(variantService.findByVariantID(av.getVariantID())).isNotNull();

        // generate some devices:
        final int NUMBER_OF_INSTALLATIONS = 10000;
        final List<Installation> devices = new ArrayList<Installation>();
        for (int i = 0; i < NUMBER_OF_INSTALLATIONS; i++) {
            Installation device = new Installation();
            device.setDeviceToken(generateFakedDeviceTokenString());
            devices.add(device);
        }

        clientInstallationService.addInstallations(av, devices);

        assertThat(clientInstallationService.findAllDeviceTokenForVariantIDByCriteria(av.getVariantID(), null, null, null)).hasSize(NUMBER_OF_INSTALLATIONS);
    }

    private String generateFakedDeviceTokenString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(UUID.randomUUID().toString());
        sb.append(UUID.randomUUID().toString());
        sb.append(UUID.randomUUID().toString());
        return sb.toString();
    }

}
