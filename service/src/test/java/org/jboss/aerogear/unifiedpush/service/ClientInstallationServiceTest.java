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

import org.jboss.aerogear.unifiedpush.api.AndroidVariant;
import org.jboss.aerogear.unifiedpush.api.Category;
import org.jboss.aerogear.unifiedpush.api.Installation;
import org.jboss.aerogear.unifiedpush.dao.ResultStreamException;
import org.jboss.aerogear.unifiedpush.dao.ResultsStream;
import org.junit.Test;

import javax.inject.Inject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class ClientInstallationServiceTest extends AbstractBaseServiceTest {

    @Inject
    private ClientInstallationService clientInstallationService;

    @Inject
    private GenericVariantService variantService;

    private AndroidVariant androidVariant;

    @Override
    protected void specificSetup() {
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

        assertThat(findAllDeviceTokenForVariantIDByCriteria(androidVariant.getVariantID(), null, null, null)).hasSize(1);

        // apply some update:
        Installation otherDevice = new Installation();
        otherDevice.setDeviceToken(generateFakedDeviceTokenString());
        otherDevice.setAlias("username");

        clientInstallationService.addInstallation(androidVariant, otherDevice);
        assertThat(findAllDeviceTokenForVariantIDByCriteria(androidVariant.getVariantID(), null, null, null)).hasSize(2);
    }

    @Test
    public void updateDevice() {
        Installation device = new Installation();
        String deviceToken = generateFakedDeviceTokenString();
        device.setDeviceToken(deviceToken);
        clientInstallationService.addInstallation(androidVariant, device);

        assertThat(findAllDeviceTokenForVariantIDByCriteria(androidVariant.getVariantID(), null, null, null)).hasSize(1);

        // apply some update:
        Installation sameDeviceDifferentRegistration = new Installation();
        sameDeviceDifferentRegistration.setDeviceToken(deviceToken);
        sameDeviceDifferentRegistration.setAlias("username");

        clientInstallationService.addInstallation(androidVariant, sameDeviceDifferentRegistration);
        assertThat(findAllDeviceTokenForVariantIDByCriteria(androidVariant.getVariantID(), null, null, null)).hasSize(1);
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
        assertThat(findAllDeviceTokenForVariantIDByCriteria(androidVariant.getVariantID(), null, null, null)).hasSize(NUMBER_OF_INSTALLATIONS);

        // add just one device:
        device = new Installation();
        device.setDeviceToken(generateFakedDeviceTokenString());
        devices.add(device);

        // run the importer again
        clientInstallationService.addInstallations(androidVariant, devices);
        assertThat(findAllDeviceTokenForVariantIDByCriteria(androidVariant.getVariantID(), null, null, null)).hasSize(NUMBER_OF_INSTALLATIONS + 1);
    }

    @Test
    public void createAndDeleteDeviceByToken() {

        Installation device = new Installation();
        device.setDeviceToken(generateFakedDeviceTokenString());

        clientInstallationService.addInstallation(androidVariant, device);
        assertThat(findAllDeviceTokenForVariantIDByCriteria(androidVariant.getVariantID(), null, null, null)).hasSize(1);

        final String singleToken = device.getDeviceToken();
        clientInstallationService.removeInstallationForVariantByDeviceToken(androidVariant.getVariantID(), singleToken);
        assertThat(findAllDeviceTokenForVariantIDByCriteria(androidVariant.getVariantID(), null, null, null)).isEmpty();
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
        assertThat(findAllDeviceTokenForVariantIDByCriteria(androidVariant.getVariantID(), null, null, null)).hasSize(NUMBER_OF_INSTALLATIONS);

        // add just one device:
        Installation device = new Installation();
        device.setDeviceToken(generateFakedDeviceTokenString());
        devices.add(device);

        // run the importer again
        clientInstallationService.addInstallations(androidVariant, devices);
        assertThat(findAllDeviceTokenForVariantIDByCriteria(androidVariant.getVariantID(), null, null, null)).hasSize(NUMBER_OF_INSTALLATIONS + 1);
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

        assertThat(findAllDeviceTokenForVariantIDByCriteria(androidVariant.getVariantID(), null, null, null)).hasSize(NUMBER_OF_INSTALLATIONS);
    }

    @Test
    public void findSingleDeviceTokenWithMultipleCategories() {

        Installation device = new Installation();
        String deviceToken = generateFakedDeviceTokenString();
        device.setDeviceToken(deviceToken);

        final Set<Category> categories = new HashSet<Category>(Arrays.asList(new Category("football"), new Category("soccer")));
        device.setCategories(categories);

        clientInstallationService.addInstallation(androidVariant, device);

        assertThat(findAllDeviceTokenForVariantIDByCriteria(androidVariant.getVariantID(), Arrays.asList("football", "soccer"), null, null)).hasSize(1);
    }

    @Test
    public void findSingleDeviceTokenWithMultipleCategoriesAndByAlias() {

        Installation device = new Installation();
        String deviceToken = generateFakedDeviceTokenString();
        device.setDeviceToken(deviceToken);
        device.setAlias("root");

        final Set<Category> categories = new HashSet<Category>(Arrays.asList(new Category("football"), new Category("soccer")));
        device.setCategories(categories);

        clientInstallationService.addInstallation(androidVariant, device);

        assertThat(findAllDeviceTokenForVariantIDByCriteria(androidVariant.getVariantID(), Arrays.asList("football", "soccer"), Arrays.asList("root"), null)).hasSize(1);
    }

    @Test
    public void findDeviceTokensWithSingleCategory() {

        Installation device1 = new Installation();
        device1.setDeviceToken(generateFakedDeviceTokenString());
        Set<Category> categories = new HashSet<Category>(Arrays.asList(new Category("football"), new Category("soccer")));
        device1.setCategories(categories);
        clientInstallationService.addInstallation(androidVariant, device1);

        Installation device2 = new Installation();
        device2.setDeviceToken(generateFakedDeviceTokenString());
        categories = new HashSet<Category>(Arrays.asList(new Category("soccer")));
        device2.setCategories(categories);
        clientInstallationService.addInstallation(androidVariant, device2);

        Installation device3 = new Installation();
        device3.setDeviceToken(generateFakedDeviceTokenString());
        categories = new HashSet<Category>(Arrays.asList(new Category("football")));
        device3.setCategories(categories);
        clientInstallationService.addInstallation(androidVariant, device3);

        final List<String> queriedTokens = findAllDeviceTokenForVariantIDByCriteria(androidVariant.getVariantID(), Arrays.asList("soccer"), null, null);

        assertThat(queriedTokens).hasSize(2);
        assertThat(queriedTokens).contains(
                device1.getDeviceToken(),
                device2.getDeviceToken()
        );
    }

    @Test
    public void findDeviceTokensWithMultipleCategories() {

        Installation device1 = new Installation();
        device1.setDeviceToken(generateFakedDeviceTokenString());
        Set<Category> categories = new HashSet<Category>(Arrays.asList(new Category("football"), new Category("soccer")));
        device1.setCategories(categories);
        clientInstallationService.addInstallation(androidVariant, device1);

        Installation device2 = new Installation();
        device2.setDeviceToken(generateFakedDeviceTokenString());
        categories = new HashSet<Category>(Arrays.asList(new Category("soccer")));
        device2.setCategories(categories);
        clientInstallationService.addInstallation(androidVariant, device2);

        Installation device3 = new Installation();
        device3.setDeviceToken(generateFakedDeviceTokenString());
        categories = new HashSet<Category>(Arrays.asList(new Category("football")));
        device3.setCategories(categories);
        clientInstallationService.addInstallation(androidVariant, device3);

        final List<String> queriedTokens = findAllDeviceTokenForVariantIDByCriteria(androidVariant.getVariantID(), Arrays.asList("soccer", "football"), null, null);

        assertThat(queriedTokens).hasSize(3);
        assertThat(queriedTokens).contains(
                device1.getDeviceToken(),
                device2.getDeviceToken(),
                device3.getDeviceToken()
        );
    }

    @Test
    public void findDeviceTokensWithoutAnyCriteria() {

        Installation device1 = new Installation();
        device1.setDeviceToken(generateFakedDeviceTokenString());
        Set<Category> categories = new HashSet<Category>(Arrays.asList(new Category("football"), new Category("soccer")));
        device1.setCategories(categories);
        clientInstallationService.addInstallation(androidVariant, device1);

        Installation device2 = new Installation();
        device2.setDeviceToken(generateFakedDeviceTokenString());
        categories = new HashSet<Category>(Arrays.asList(new Category("soccer")));
        device2.setCategories(categories);
        clientInstallationService.addInstallation(androidVariant, device2);

        Installation device3 = new Installation();
        device3.setDeviceToken(generateFakedDeviceTokenString());
        categories = new HashSet<Category>(Arrays.asList(new Category("football")));
        device3.setCategories(categories);
        clientInstallationService.addInstallation(androidVariant, device3);

        final List<String> queriedTokens = findAllDeviceTokenForVariantIDByCriteria(androidVariant.getVariantID(), null, null, null);

        assertThat(queriedTokens).hasSize(3);
        assertThat(queriedTokens).contains(
                device1.getDeviceToken(),
                device2.getDeviceToken(),
                device3.getDeviceToken()
        );
    }

    @Test
    public void findDeviceTokensByAlias() {

        Installation device = new Installation();
        String deviceToken = generateFakedDeviceTokenString();
        device.setDeviceToken(deviceToken);
        device.setAlias("root");
        clientInstallationService.addInstallation(androidVariant, device);

        // apply some update:
        Installation otherDevice = new Installation();
        otherDevice.setDeviceToken(generateFakedDeviceTokenString());
        otherDevice.setAlias("root");
        clientInstallationService.addInstallation(androidVariant, otherDevice);

        assertThat(findAllDeviceTokenForVariantIDByCriteria(androidVariant.getVariantID(), null, Arrays.asList("root"), null)).hasSize(2);
    }



    private String generateFakedDeviceTokenString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(UUID.randomUUID().toString());
        sb.append(UUID.randomUUID().toString());
        sb.append(UUID.randomUUID().toString());
        return sb.toString();
    }

    private List<String> findAllDeviceTokenForVariantIDByCriteria(String variantID, List<String> categories, List<String> aliases, List<String> deviceTypes) {
        try {
            ResultsStream<String> tokenStream = clientInstallationService.findAllDeviceTokenForVariantIDByCriteria(variantID, categories, aliases, deviceTypes, Integer.MAX_VALUE, null).executeQuery();
            List<String> list = new ArrayList<String>();
            while (tokenStream.next()) {
                list.add(tokenStream.get());
            }
            return list;
        } catch (ResultStreamException e) {
            throw new IllegalStateException(e);
        }
    }

}
