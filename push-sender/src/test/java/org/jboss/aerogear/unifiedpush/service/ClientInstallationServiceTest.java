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
import org.jboss.aerogear.unifiedpush.api.iOSVariant;
import org.jboss.aerogear.unifiedpush.dao.ResultStreamException;
import org.jboss.aerogear.unifiedpush.dao.ResultsStream;
import org.jboss.aerogear.unifiedpush.service.impl.ClientInstallationServiceImpl;
import org.jboss.aerogear.unifiedpush.service.util.FCMTopicManager;
import org.jboss.aerogear.unifiedpush.test.archive.UnifiedPushArchive;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.json.simple.parser.ParseException;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(Arquillian.class)
public class ClientInstallationServiceTest extends AbstractBaseServiceTest {

    private static byte[] certificate = toByteArray(ClientInstallationServiceTest.class.getResourceAsStream("/cert/certificate.p12"));

    @Deployment
    public static WebArchive archive() {
        return UnifiedPushArchive.forTestClass(ClientInstallationServiceTest.class)
                .withUtils()
                .withMessageModel()
                .withMockito()
                .withMessaging()
                .withDAOs().forServiceTests()
                .withServices()
                .withApi()
                .withUtils()
                .addClass(AbstractBaseServiceTest.class)
                .addClass(EntityManagerProducer.class)
                .addPackage(FCMTopicManager.class.getPackage())
                .addPackage(ParseException.class.getPackage())
                .addPackage(ClientInstallationServiceImpl.class.getPackage())
                .addAsResource("cert/certificate.p12","cert/certificate.p12")
                .addMavenDependencies("org.assertj:assertj-core")
                .forServiceTests()
                .as(WebArchive.class);
    }


    @Inject
    private ClientInstallationService clientInstallationService;

    @Inject
    private GenericVariantService variantService;

    private AndroidVariant androidVariant;

    @Override
    protected void specificSetup() {
        // setup a variant:
        androidVariant = new AndroidVariant();
        androidVariant.setGoogleKey("XYZQWEAWSvw:GGG91bGd6o9D-tU9JJJyvlL1iXibUde4fckYLG-LOLebCnB07Z1kTSycOC3a7HHxP1JFv6S_xxxxxMWgXQ2hZZIQoo3o4Jv0JbzPdNg4x4b0MRdjPM4M4cFOxBF4l4lozBRx4looPYso");
        androidVariant.setName("Android");
        androidVariant.setDeveloper("me");
        variantService.addVariant(androidVariant);
    }

    @Test
    public void testLowerCaseForIOS() throws IOException, ExecutionException, InterruptedException {
        iOSVariant iOSVariant = new iOSVariant();
        iOSVariant.setCertificate(certificate);
        iOSVariant.setPassphrase("12345678");
        iOSVariant.setName("iOS Variant Name");
        variantService.addVariant(iOSVariant);

        Installation device = new Installation();
        device.setAlias("SomeAlias");
        String deviceToken = generateFakedDeviceTokenString().toUpperCase();
        device.setDeviceToken(deviceToken);

        clientInstallationService.addInstallation(iOSVariant, device).get();

        assertThat(clientInstallationService.findInstallationForVariantByDeviceToken(iOSVariant.getVariantID(), deviceToken)).isNull();
        assertThat(clientInstallationService.findInstallationForVariantByDeviceToken(iOSVariant.getVariantID(), deviceToken.toLowerCase())).isNotNull();
    }

    @Test
    public void registerDevices() throws ExecutionException, InterruptedException {

        Installation device = new Installation();
        String deviceToken = generateFakedDeviceTokenString().toUpperCase();
        device.setDeviceToken(deviceToken);
        clientInstallationService.addInstallation(androidVariant, device).get();

        assertThat(findAllDeviceTokenForVariantIDByCriteria(androidVariant.getVariantID(), null, null, null)).hasSize(1);

        // apply some update:
        Installation otherDevice = new Installation();
        otherDevice.setDeviceToken(generateFakedDeviceTokenString());
        otherDevice.setAlias("username");

        clientInstallationService.addInstallation(androidVariant, otherDevice).get();
        assertThat(findAllDeviceTokenForVariantIDByCriteria(androidVariant.getVariantID(), null, null, null)).hasSize(2);
    }

    @Test
    public void registerDevicesWithCategories() throws ExecutionException, InterruptedException {

        Installation device = new Installation();
        String deviceToken = generateFakedDeviceTokenString().toUpperCase();
        device.setDeviceToken(deviceToken);
        final Set<Category> categories = new HashSet<>(Arrays.asList(new Category("football"), new Category("football")));
        device.setCategories(categories);
        clientInstallationService.addInstallation(androidVariant, device).get();

        assertThat(clientInstallationService.findInstallationForVariantByDeviceToken(androidVariant.getVariantID(),deviceToken).getCategories()).hasSize(1);
    }

    @Test
    public void registerTwoDevicesWithDifferentCategories() throws ExecutionException, InterruptedException {
        Installation device = new Installation();
        String deviceToken = generateFakedDeviceTokenString();
        device.setDeviceToken(deviceToken);

        Set<Category> categories = new HashSet<>(Arrays.asList(new Category("football"), new Category("soccer")));
        device.setCategories(categories);

        device.setVariant(androidVariant);

        clientInstallationService.addInstallation(androidVariant, device).get();
        assertThat(clientInstallationService.findInstallationForVariantByDeviceToken(androidVariant.getVariantID(),deviceToken).getCategories()).hasSize(2);

        // second device, with slightly different metadata
        device = new Installation();
        deviceToken = generateFakedDeviceTokenString().toUpperCase();
        device.setDeviceToken(deviceToken);
        categories = new HashSet<>(Arrays.asList(new Category("lame"), new Category("football")));
        device.setCategories(categories);
        clientInstallationService.addInstallation(androidVariant, device).get();
        assertThat(clientInstallationService.findInstallationForVariantByDeviceToken(androidVariant.getVariantID(),deviceToken).getCategories()).hasSize(2);

        assertThat(
                clientInstallationService.findInstallationForVariantByDeviceToken(androidVariant.getVariantID(),deviceToken).getCategories())
                .extracting("name")
                .contains("football","lame")
                .doesNotContain("soccer");
    }

    @Test
    public void removeOneCategoryFromPreviouslyRegisteredDevice() throws ExecutionException, InterruptedException {
        Installation device = new Installation();
        String deviceToken = generateFakedDeviceTokenString();
        device.setDeviceToken(deviceToken);

        Set<Category> categories = new HashSet<>(Arrays.asList(new Category("football"), new Category("soccer")));
        device.setCategories(categories);

        device.setVariant(androidVariant);

        clientInstallationService.addInstallation(androidVariant, device).get();
        assertThat(clientInstallationService.findInstallationForVariantByDeviceToken(androidVariant.getVariantID(),deviceToken).getCategories()).hasSize(2);

        // same device, with slightly different metadata
        device = new Installation();
        device.setDeviceToken(deviceToken);
        categories = new HashSet<>(Arrays.asList(new Category("football")));
        device.setCategories(categories);
        clientInstallationService.addInstallation(androidVariant, device).get();
        assertThat(clientInstallationService.findInstallationForVariantByDeviceToken(androidVariant.getVariantID(),deviceToken).getCategories()).hasSize(1);

        assertThat(
                clientInstallationService.findInstallationForVariantByDeviceToken(androidVariant.getVariantID(),deviceToken).getCategories())
                .extracting("name")
                .contains("football")
                .doesNotContain("soccer");
    }


    @Test
    public void registerDevicesAndUpdateWithCategories() throws ExecutionException, InterruptedException {

        Installation device = new Installation();
        String deviceToken = generateFakedDeviceTokenString().toUpperCase();
        device.setDeviceToken(deviceToken);
        clientInstallationService.addInstallation(androidVariant, device).get();

        assertThat(clientInstallationService.findInstallationForVariantByDeviceToken(androidVariant.getVariantID(),deviceToken).getCategories()).isEmpty();

        device = new Installation();
        device.setDeviceToken(deviceToken);
        final Set<Category> categories = new HashSet<>(Arrays.asList(new Category("football"), new Category("football")));
        device.setCategories(categories);

        clientInstallationService.addInstallation(androidVariant, device).get();

        assertThat(clientInstallationService.findInstallationForVariantByDeviceToken(androidVariant.getVariantID(),deviceToken).getCategories()).hasSize(1);
    }


    @Test
    public void updateDevice() throws ExecutionException, InterruptedException {
        Installation device = new Installation();
        String deviceToken = generateFakedDeviceTokenString();
        device.setDeviceToken(deviceToken);
        clientInstallationService.addInstallation(androidVariant, device).get();
        assertThat(findAllDeviceTokenForVariantIDByCriteria(androidVariant.getVariantID(), null, null, null)).hasSize(1);

        androidVariant = (AndroidVariant) variantService.findByVariantID(androidVariant.getVariantID());
        // apply some update:
        Installation sameDeviceDifferentRegistration = new Installation();
        sameDeviceDifferentRegistration.setDeviceToken(deviceToken);
        sameDeviceDifferentRegistration.setAlias("username");

        clientInstallationService.addInstallation(androidVariant, sameDeviceDifferentRegistration).get();
        assertThat(findAllDeviceTokenForVariantIDByCriteria(androidVariant.getVariantID(), null, null, null)).hasSize(1);
    }

    @Test
    public void importDevicesWithAndWithoutTokenDuplicates() throws ExecutionException, InterruptedException {

        // generate some devices with token:
        final int NUMBER_OF_INSTALLATIONS = 5;
        final List<Installation> devices = new ArrayList<>();
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

        clientInstallationService.addInstallations(androidVariant, devices).get();

        // but they got ignored:
        assertThat(findAllDeviceTokenForVariantIDByCriteria(androidVariant.getVariantID(), null, null, null)).hasSize(NUMBER_OF_INSTALLATIONS);

        // add just one device:
        device = new Installation();
        device.setDeviceToken(generateFakedDeviceTokenString());
        devices.add(device);

        // run the importer again
        clientInstallationService.addInstallations(androidVariant, devices).get();
        assertThat(findAllDeviceTokenForVariantIDByCriteria(androidVariant.getVariantID(), null, null, null)).hasSize(NUMBER_OF_INSTALLATIONS + 1);
    }

    @Test
    public void createAndDeleteDeviceByToken() throws ExecutionException, InterruptedException {

        Installation device = new Installation();
        device.setDeviceToken(generateFakedDeviceTokenString());

        clientInstallationService.addInstallation(androidVariant, device).get();
        assertThat(findAllDeviceTokenForVariantIDByCriteria(androidVariant.getVariantID(), null, null, null)).hasSize(1);

        final String singleToken = device.getDeviceToken();
        clientInstallationService.removeInstallationForVariantByDeviceToken(androidVariant.getVariantID(), singleToken).get();
        assertThat(findAllDeviceTokenForVariantIDByCriteria(androidVariant.getVariantID(), null, null, null)).isEmpty();
    }

    @Test
    public void importDevicesWithoutDuplicates() throws ExecutionException, InterruptedException {

        // generate some devices:
        final int NUMBER_OF_INSTALLATIONS = 5;
        final List<Installation> devices = new ArrayList<>();
        for (int i = 0; i < NUMBER_OF_INSTALLATIONS; i++) {
            Installation device = new Installation();
            device.setDeviceToken(generateFakedDeviceTokenString());
            devices.add(device);
        }

        clientInstallationService.addInstallations(androidVariant, devices).get();
        assertThat(findAllDeviceTokenForVariantIDByCriteria(androidVariant.getVariantID(), null, null, null)).hasSize(NUMBER_OF_INSTALLATIONS);

        // add just one device:
        Installation device = new Installation();
        device.setDeviceToken(generateFakedDeviceTokenString());
        devices.add(device);

        // run the importer again
        clientInstallationService.addInstallations(androidVariant, devices).get();
        assertThat(findAllDeviceTokenForVariantIDByCriteria(androidVariant.getVariantID(), null, null, null)).hasSize(NUMBER_OF_INSTALLATIONS + 1);
    }

    @Test
    public void importDevices() throws ExecutionException, InterruptedException {

        // generate some devices:
        final int NUMBER_OF_INSTALLATIONS = 100000;
        final List<Installation> devices = new ArrayList<>();
        for (int i = 0; i < NUMBER_OF_INSTALLATIONS; i++) {
            Installation device = new Installation();
            device.setDeviceToken(generateFakedDeviceTokenString());
            devices.add(device);
        }

        clientInstallationService.addInstallations(androidVariant, devices).get();

        assertThat(findAllDeviceTokenForVariantIDByCriteria(androidVariant.getVariantID(), null, null, null)).hasSize(NUMBER_OF_INSTALLATIONS);
    }

    @Test
    public void findSingleDeviceTokenWithMultipleCategories() throws ExecutionException, InterruptedException {

        Installation device = new Installation();
        String deviceToken = generateFakedDeviceTokenString();
        device.setDeviceToken(deviceToken);

        final Set<Category> categories = new HashSet<>(Arrays.asList(new Category("football"), new Category("soccer")));
        device.setCategories(categories);

        device.setVariant(androidVariant);

        clientInstallationService.addInstallation(androidVariant, device).get();

        assertThat(findAllDeviceTokenForVariantIDByCriteria(androidVariant.getVariantID(), Arrays.asList("football", "soccer"), null, null)).hasSize(1);
    }

    @Test
    public void findSingleDeviceTokenWithMultipleCategoriesAndByAlias() throws ExecutionException, InterruptedException {

        Installation device = new Installation();
        String deviceToken = generateFakedDeviceTokenString();
        device.setDeviceToken(deviceToken);
        device.setAlias("root");

        final Set<Category> categories = new HashSet<>(Arrays.asList(new Category("football"), new Category("soccer")));
        device.setCategories(categories);

        device.setVariant(androidVariant);

        clientInstallationService.addInstallation(androidVariant, device).get();

        assertThat(findAllDeviceTokenForVariantIDByCriteria(androidVariant.getVariantID(), Arrays.asList("football", "soccer"), Arrays.asList("root"), null)).hasSize(1);
    }

    @Test
    public void updateDeviceByRemovingCategory() throws ExecutionException, InterruptedException {
        //sessionContext.activate();

        Installation device = new Installation();
        String deviceToken = generateFakedDeviceTokenString();
        device.setDeviceToken(deviceToken);
        device.setAlias("root");

        final Set<Category> categories = new HashSet<>(Arrays.asList(new Category("football"), new Category("soccer")));
        device.setCategories(categories);

        device.setVariant(androidVariant);

        clientInstallationService.addInstallation(androidVariant, device).get();
        assertThat(findAllDeviceTokenForVariantIDByCriteria(androidVariant.getVariantID(), Arrays.asList("football", "soccer"), Arrays.asList("root"), null)).hasSize(1);
        assertThat(clientInstallationService.findInstallationForVariantByDeviceToken(androidVariant.getVariantID(), deviceToken).getCategories()).hasSize(2);

        // simulate a post WITHOUT the categories metadataad
        device = new Installation();
        device.setDeviceToken(deviceToken);
        device.setAlias("root");

        // and update
        clientInstallationService.addInstallation(androidVariant, device).get();
        assertThat(clientInstallationService.findInstallationForVariantByDeviceToken(androidVariant.getVariantID(), deviceToken).getCategories()).isEmpty();
    }

    @Test
    public void findDeviceTokensWithSingleCategory() throws ExecutionException, InterruptedException {

        Installation device1 = new Installation();
        device1.setDeviceToken(generateFakedDeviceTokenString());
        Set<Category> categories = new HashSet<>(Arrays.asList(new Category("football"), new Category("soccer")));
        device1.setCategories(categories);
        device1.setVariant(androidVariant);
        clientInstallationService.addInstallation(androidVariant, device1).get();

        Installation device2 = new Installation();
        device2.setDeviceToken(generateFakedDeviceTokenString());
        categories = new HashSet<>(Arrays.asList(new Category("soccer")));
        device2.setCategories(categories);
        device2.setVariant(androidVariant);
        clientInstallationService.addInstallation(androidVariant, device2).get();

        Installation device3 = new Installation();
        device3.setDeviceToken(generateFakedDeviceTokenString());
        categories = new HashSet<>(Arrays.asList(new Category("football")));
        device3.setCategories(categories);
        device3.setVariant(androidVariant);
        clientInstallationService.addInstallation(androidVariant, device3).get();

        final List<String> queriedTokens = findAllDeviceTokenForVariantIDByCriteria(androidVariant.getVariantID(), Arrays.asList("soccer"), null, null);

        assertThat(queriedTokens).hasSize(2);
        assertThat(queriedTokens).contains(
                device1.getDeviceToken(),
                device2.getDeviceToken()
        );
    }

    @Test
    public void findDeviceTokensWithMultipleCategories() throws ExecutionException, InterruptedException {

        Installation device1 = new Installation();
        device1.setDeviceToken(generateFakedDeviceTokenString());
        Set<Category> categories = new HashSet<>(Arrays.asList(new Category("football"), new Category("soccer")));
        device1.setCategories(categories);
        device1.setVariant(androidVariant);
        clientInstallationService.addInstallation(androidVariant, device1).get();

        Installation device2 = new Installation();
        device2.setDeviceToken(generateFakedDeviceTokenString());
        categories = new HashSet<>(Arrays.asList(new Category("soccer")));
        device2.setCategories(categories);
        device2.setVariant(androidVariant);
        clientInstallationService.addInstallation(androidVariant, device2).get();

        Installation device3 = new Installation();
        device3.setDeviceToken(generateFakedDeviceTokenString());
        categories = new HashSet<>(Arrays.asList(new Category("football")));
        device3.setCategories(categories);
        device3.setVariant(androidVariant);
        clientInstallationService.addInstallation(androidVariant, device3).get();

        final List<String> queriedTokens = findAllDeviceTokenForVariantIDByCriteria(androidVariant.getVariantID(), Arrays.asList("soccer", "football"), null, null);

        assertThat(queriedTokens).hasSize(3);
        assertThat(queriedTokens).contains(
                device1.getDeviceToken(),
                device2.getDeviceToken(),
                device3.getDeviceToken()
        );
    }

    @Test
    public void findDeviceTokensWithoutAnyCriteria() throws ExecutionException, InterruptedException {

        Installation device1 = new Installation();
        device1.setDeviceToken(generateFakedDeviceTokenString());
        Set<Category> categories = new HashSet<>(Arrays.asList(new Category("football"), new Category("soccer")));
        device1.setCategories(categories);
        clientInstallationService.addInstallation(androidVariant, device1).get();

        Installation device2 = new Installation();
        device2.setDeviceToken(generateFakedDeviceTokenString());
        categories = new HashSet<>(Arrays.asList(new Category("soccer")));
        device2.setCategories(categories);
        clientInstallationService.addInstallation(androidVariant, device2).get();

        Installation device3 = new Installation();
        device3.setDeviceToken(generateFakedDeviceTokenString());
        categories = new HashSet<>(Arrays.asList(new Category("football")));
        device3.setCategories(categories);
        clientInstallationService.addInstallation(androidVariant, device3).get();

        Installation device4 = new Installation();
        device4.setDeviceToken("01234567891:"+generateFakedDeviceTokenString());
        categories = new HashSet<>(Arrays.asList(new Category("football")));
        device4.setCategories(categories);
        clientInstallationService.addInstallation(androidVariant, device4).get();

        final List<String> queriedTokens = findAllDeviceTokenForVariantIDByCriteria(androidVariant.getVariantID(), null, null, null);

        assertThat(queriedTokens).hasSize(4);
        assertThat(queriedTokens).contains(
                device1.getDeviceToken(),
                device2.getDeviceToken(),
                device3.getDeviceToken(),
                device4.getDeviceToken()
        );
        final List<String> legacyTokenz = findAllOldGoogleCloudMessagingDeviceTokenForVariantIDByCriteria(androidVariant.getVariantID(), null, null, null);

        assertThat(legacyTokenz).hasSize(3);
        assertThat(legacyTokenz).contains(
                device1.getDeviceToken(),
                device2.getDeviceToken(),
                device3.getDeviceToken()
        );
        assertThat(legacyTokenz).doesNotContain(
                device4.getDeviceToken()
        );
    }

    @Test
    public void findDeviceTokensByAlias() throws ExecutionException, InterruptedException {

        Installation device = new Installation();
        String deviceToken = generateFakedDeviceTokenString();
        device.setDeviceToken(deviceToken);
        device.setAlias("root");
        clientInstallationService.addInstallation(androidVariant, device).get();

        // apply some update:
        Installation otherDevice = new Installation();
        otherDevice.setDeviceToken(generateFakedDeviceTokenString());
        otherDevice.setAlias("root");
        clientInstallationService.addInstallation(androidVariant, otherDevice).get();

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
            List<String> list = new ArrayList<>();
            while (tokenStream.next()) {
                list.add(tokenStream.get());
            }
            return list;
        } catch (ResultStreamException e) {
            throw new IllegalStateException(e);
        }
    }

    private List<String> findAllOldGoogleCloudMessagingDeviceTokenForVariantIDByCriteria(String variantID, List<String> categories, List<String> aliases, List<String> deviceTypes) {
        try {
            ResultsStream<String> tokenStream = clientInstallationService.findAllOldGoogleCloudMessagingDeviceTokenForVariantIDByCriteria(variantID, categories, aliases, deviceTypes, Integer.MAX_VALUE, null).executeQuery();
            List<String> list = new ArrayList<>();
            while (tokenStream.next()) {
                list.add(tokenStream.get());
            }
            return list;
        } catch (ResultStreamException e) {
            throw new IllegalStateException(e);
        }
    }

    // simple util, borrowed from AG Crypto
    private static byte[] toByteArray(InputStream file) {
        try {
            int n;
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];

            while (-1 != (n = file.read(buffer))) {
                bos.write(buffer, 0, n);
            }
            return bos.toByteArray();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

}
