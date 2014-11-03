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
import org.apache.openejb.mockito.MockitoInjector;
import org.apache.openejb.testing.MockInjector;
import org.apache.openejb.testing.Module;
import org.jboss.aerogear.unifiedpush.api.AndroidVariant;
import org.jboss.aerogear.unifiedpush.api.Installation;
import org.jboss.aerogear.unifiedpush.jpa.dao.impl.JPACategoryDao;
import org.jboss.aerogear.unifiedpush.jpa.dao.impl.JPAInstallationDao;
import org.jboss.aerogear.unifiedpush.jpa.dao.impl.JPAPushApplicationDao;
import org.jboss.aerogear.unifiedpush.jpa.dao.impl.JPAPushMessageInformationDao;
import org.jboss.aerogear.unifiedpush.jpa.dao.impl.JPAVariantDao;
import org.jboss.aerogear.unifiedpush.service.impl.ClientInstallationServiceImpl;
import org.jboss.aerogear.unifiedpush.service.impl.GenericVariantServiceImpl;
import org.jboss.aerogear.unifiedpush.service.impl.PushApplicationServiceImpl;
import org.jboss.aerogear.unifiedpush.service.impl.PushSearchByDeveloperServiceImpl;
import org.jboss.aerogear.unifiedpush.service.impl.PushSearchServiceImpl;
import org.jboss.aerogear.unifiedpush.service.impl.SearchManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.representations.AccessToken;
import org.mockito.Mock;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(ApplicationComposer.class)
public class ClientInstallationServiceTest extends AbstractBaseServiceTest {


    @Inject
    private ClientInstallationService clientInstallationService;

    @Inject
    private GenericVariantService variantService;

    @Inject
    private SearchManager searchManager;

    @Inject
    private PushApplicationServiceImpl pushApplicationService;

    @Inject
    private PushSearchByDeveloperServiceImpl searchApplicationService;

    private AndroidVariant androidVariant;

    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private KeycloakSecurityContext context;

    @Mock
    private KeycloakPrincipal keycloakPrincipal;

    @MockInjector
    public Class<?> mockitoInjector() {
        return MockitoInjector.class;
    }

    @Module
    public Beans getBeans() {
        final Beans beans = new Beans();
        beans.addManagedClass(ClientInstallationServiceImpl.class);
        beans.addManagedClass(JPAPushMessageInformationDao.class);
        beans.addManagedClass(JPAInstallationDao.class);
        beans.addManagedClass(JPACategoryDao.class);
        beans.addManagedClass(GenericVariantServiceImpl.class);
        beans.addManagedClass(JPAVariantDao.class);
        beans.addManagedClass(PushSearchByDeveloperServiceImpl.class);
        beans.addManagedClass(PushApplicationServiceImpl.class);
        beans.addManagedClass(JPAPushApplicationDao.class);
        beans.addManagedClass(PushSearchServiceImpl.class);
        beans.addManagedClass(SearchManager.class);

        return beans;
    }

    @Before
    public void setup() {

        AccessToken token = new AccessToken();
        token.setPreferredUsername("admin");
        when(context.getToken()).thenReturn(token);
        when(keycloakPrincipal.getKeycloakSecurityContext()).thenReturn(context);
        when(httpServletRequest.getUserPrincipal()).thenReturn(keycloakPrincipal);

        searchManager.setHttpServletRequest(httpServletRequest);

        // setup a variant:
        androidVariant = new AndroidVariant();
        androidVariant.setGoogleKey("Key");
        androidVariant.setName("Android");
        androidVariant.setDeveloper("me");
        variantService.addVariant(androidVariant);
    }

    @Test
    public void registerDevices() {

        Installation device = new Installation();
        String deviceToken = generateFakedDeviceTokenString();
        device.setDeviceToken(deviceToken);
        clientInstallationService.addInstallation(androidVariant, device);

        assertThat(clientInstallationService.findAllDeviceTokenForVariantIDByCriteria(androidVariant.getVariantID(), null, null, null)).hasSize(1);

        // apply some update:
        Installation otherDevice = new Installation();
        otherDevice.setDeviceToken(generateFakedDeviceTokenString());
        otherDevice.setAlias("username");

        clientInstallationService.addInstallation(androidVariant, otherDevice);
        assertThat(clientInstallationService.findAllDeviceTokenForVariantIDByCriteria(androidVariant.getVariantID(), null, null, null)).hasSize(2);
    }

    @Test
    public void updateDevice() {
        Installation device = new Installation();
        String deviceToken = generateFakedDeviceTokenString();
        device.setDeviceToken(deviceToken);
        clientInstallationService.addInstallation(androidVariant, device);

        assertThat(clientInstallationService.findAllDeviceTokenForVariantIDByCriteria(androidVariant.getVariantID(), null, null, null)).hasSize(1);

        // apply some update:
        Installation sameDeviceDifferentRegistration = new Installation();
        sameDeviceDifferentRegistration.setDeviceToken(deviceToken);
        sameDeviceDifferentRegistration.setAlias("username");

        clientInstallationService.addInstallation(androidVariant, sameDeviceDifferentRegistration);
        assertThat(clientInstallationService.findAllDeviceTokenForVariantIDByCriteria(androidVariant.getVariantID(), null, null, null)).hasSize(1);
    }

    @Test
    public void importDevicesWithAndWithoutTokenDuplicates() {

        // generate some devices with token:
        final int NUMBER_OF_INSTALLATIONS = 5;
        final List<Installation> devices = new ArrayList<Installation>();
        for (int i = 0; i < NUMBER_OF_INSTALLATIONS; i++) {
            Installation device = new Installation();
            device.setDeviceToken(generateFakedDeviceTokenString());
            devices.add(device);
        }

        // add two more with invalid token:
        Installation device = new Installation();
        devices.add(device);

        device = new Installation();
        device.setDeviceToken("");
        devices.add(device);


        // a few invalid ones....
        assertThat(devices).hasSize(NUMBER_OF_INSTALLATIONS + 2);

        clientInstallationService.addInstallations(androidVariant, devices);

        // but they got ignored:
        assertThat(clientInstallationService.findAllDeviceTokenForVariantIDByCriteria(androidVariant.getVariantID(), null, null, null)).hasSize(NUMBER_OF_INSTALLATIONS);

        // add just one device:
        device = new Installation();
        device.setDeviceToken(generateFakedDeviceTokenString());
        devices.add(device);

        // run the importer again
        clientInstallationService.addInstallations(androidVariant, devices);
        assertThat(clientInstallationService.findAllDeviceTokenForVariantIDByCriteria(androidVariant.getVariantID(), null, null, null)).hasSize(NUMBER_OF_INSTALLATIONS + 1);
    }



    @Test
    public void importDevicesWithoutDuplicates() {

        // generate some devices:
        final int NUMBER_OF_INSTALLATIONS = 5;
        final List<Installation> devices = new ArrayList<Installation>();
        for (int i = 0; i < NUMBER_OF_INSTALLATIONS; i++) {
            Installation device = new Installation();
            device.setDeviceToken(generateFakedDeviceTokenString());
            devices.add(device);
        }

        clientInstallationService.addInstallations(androidVariant, devices);
        assertThat(clientInstallationService.findAllDeviceTokenForVariantIDByCriteria(androidVariant.getVariantID(), null, null, null)).hasSize(NUMBER_OF_INSTALLATIONS);

        // add just one device:
        Installation device = new Installation();
        device.setDeviceToken(generateFakedDeviceTokenString());
        devices.add(device);

        // run the importer again
        clientInstallationService.addInstallations(androidVariant, devices);
        assertThat(clientInstallationService.findAllDeviceTokenForVariantIDByCriteria(androidVariant.getVariantID(), null, null, null)).hasSize(NUMBER_OF_INSTALLATIONS + 1);
    }

    @Test
    public void importDevices() {

        // generate some devices:
        final int NUMBER_OF_INSTALLATIONS = 100000;
        final List<Installation> devices = new ArrayList<Installation>();
        for (int i = 0; i < NUMBER_OF_INSTALLATIONS; i++) {
            Installation device = new Installation();
            device.setDeviceToken(generateFakedDeviceTokenString());
            devices.add(device);
        }

        clientInstallationService.addInstallations(androidVariant, devices);

        assertThat(clientInstallationService.findAllDeviceTokenForVariantIDByCriteria(androidVariant.getVariantID(), null, null, null)).hasSize(NUMBER_OF_INSTALLATIONS);
    }

    private String generateFakedDeviceTokenString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(UUID.randomUUID().toString());
        sb.append(UUID.randomUUID().toString());
        sb.append(UUID.randomUUID().toString());
        return sb.toString();
    }

}
