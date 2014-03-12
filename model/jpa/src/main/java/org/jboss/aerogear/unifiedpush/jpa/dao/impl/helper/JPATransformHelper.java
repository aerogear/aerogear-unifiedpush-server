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
package org.jboss.aerogear.unifiedpush.jpa.dao.impl.helper;

import org.jboss.aerogear.unifiedpush.api.AndroidVariant;
import org.jboss.aerogear.unifiedpush.api.ChromePackagedAppVariant;
import org.jboss.aerogear.unifiedpush.api.Installation;
import org.jboss.aerogear.unifiedpush.api.PushApplication;
import org.jboss.aerogear.unifiedpush.api.SimplePushVariant;
import org.jboss.aerogear.unifiedpush.api.Variant;
import org.jboss.aerogear.unifiedpush.api.iOSVariant;
import org.jboss.aerogear.unifiedpush.model.jpa.AbstractVariantEntity;
import org.jboss.aerogear.unifiedpush.model.jpa.AndroidVariantEntity;
import org.jboss.aerogear.unifiedpush.model.jpa.ChromePackagedAppVariantEntity;
import org.jboss.aerogear.unifiedpush.model.jpa.InstallationEntity;
import org.jboss.aerogear.unifiedpush.model.jpa.PushApplicationEntity;
import org.jboss.aerogear.unifiedpush.model.jpa.SimplePushVariantEntity;
import org.jboss.aerogear.unifiedpush.model.jpa.iOSVariantEntity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class JPATransformHelper {

    private JPATransformHelper() {

    }

    public static AbstractVariantEntity toEntity(Variant variant) {

        AbstractVariantEntity entity = null;


        switch (variant.getType())  {
            case ANDROID:
                entity = new AndroidVariantEntity();

                ((AndroidVariantEntity) entity).setGoogleKey(((AndroidVariant) variant).getGoogleKey() );
                ((AndroidVariantEntity) entity).setProjectNumber(((AndroidVariant) variant).getProjectNumber());

                break;
            case IOS:
                entity = new iOSVariantEntity();

                ((iOSVariantEntity) entity).setCertificate(((iOSVariant) variant).getCertificate() );
                ((iOSVariantEntity) entity).setPassphrase(((iOSVariant) variant).getPassphrase());
                ((iOSVariantEntity) entity).setProduction(((iOSVariant) variant).isProduction());


                break;
            case CHROME_PACKAGED_APP:
                entity = new ChromePackagedAppVariantEntity();

                ((ChromePackagedAppVariantEntity) entity).setClientId(((ChromePackagedAppVariant) variant).getClientId());
                ((ChromePackagedAppVariantEntity) entity).setClientSecret(((ChromePackagedAppVariant) variant).getClientSecret());
                ((ChromePackagedAppVariantEntity) entity).setRefreshToken(((ChromePackagedAppVariant) variant).getRefreshToken());

                break;
            case SIMPLE_PUSH:
                entity = new SimplePushVariantEntity();

                break;
        }
        // the 'generic' part:
        entity.setId(variant.getId());
        entity.setDeveloper(variant.getDeveloper());
        entity.setName(variant.getName());
        entity.setDescription(variant.getDescription());
        entity.setVariantID(variant.getVariantID());
        entity.setSecret(variant.getSecret());

        final Set<Installation> installations = variant.getInstallations();
        if (! installations.isEmpty()) {

            final Set<InstallationEntity> installationEntities = new HashSet<InstallationEntity>();

            for (Installation installation : installations) {

                // copy all the things and stash it:
                InstallationEntity installationEntity= toEntity(installation );
                installationEntities.add(installationEntity);
            }

            entity.setInstallations(installationEntities);
        }

        return  entity;
    }



    public static Variant fromEntity(AbstractVariantEntity entity) {
        if (entity == null) {
            return null;
        }


        // the specific part...
        Variant variant = null;

        switch (entity.getType()) {
            case ANDROID:
                variant = new AndroidVariant();

                ((AndroidVariant) variant).setGoogleKey(((AndroidVariantEntity) entity).getGoogleKey() );
                ((AndroidVariant) variant).setProjectNumber(((AndroidVariantEntity) entity).getProjectNumber());

                break;
            case IOS:
                variant = new iOSVariant();

                ((iOSVariant) variant).setCertificate(((iOSVariantEntity) entity).getCertificate() );
                ((iOSVariant) variant).setPassphrase(((iOSVariantEntity) entity).getPassphrase());
                ((iOSVariant) variant).setProduction(((iOSVariantEntity) entity).isProduction());

                break;
            case CHROME_PACKAGED_APP:
                variant = new ChromePackagedAppVariant();
                ((ChromePackagedAppVariant) variant).setClientId(((ChromePackagedAppVariantEntity) entity).getClientId());
                ((ChromePackagedAppVariant) variant).setClientSecret(((ChromePackagedAppVariantEntity) entity).getClientSecret());
                ((ChromePackagedAppVariant) variant).setRefreshToken(((ChromePackagedAppVariantEntity) entity).getRefreshToken());

                break;
            case SIMPLE_PUSH:
                variant = new SimplePushVariant();

                break;
        }

        // the 'generic' part:
        variant.setId(entity.getId());
        variant.setDeveloper(entity.getDeveloper());
        variant.setName(entity.getName());
        variant.setDescription(entity.getDescription());
        variant.setVariantID(entity.getVariantID());
        variant.setSecret(entity.getSecret());

        final Set<InstallationEntity> installationEntities = entity.getInstallations();
        if (! installationEntities.isEmpty()) {

            final Set<Installation> installations = new HashSet<Installation>();

            for (InstallationEntity installationEntity : installationEntities) {

                // copy all the things and stash it:
                Installation installation = fromEntity(installationEntity);
                installations.add(installation);
            }

            variant.setInstallations(installations);
        }
        return variant;
    }


    public static Installation fromEntity(InstallationEntity installationEntity) {
        if (installationEntity == null) {
            return null;
        }

        Installation installation = new Installation();
        installation.setId(installationEntity.getId());
        installation.setDeviceToken(installationEntity.getDeviceToken());
        installation.setPlatform(installationEntity.getPlatform());
        installation.setOsVersion(installationEntity.getOsVersion());
        installation.setSimplePushEndpoint(installationEntity.getSimplePushEndpoint());
        installation.setEnabled(installationEntity.isEnabled());
        installation.setAlias(installationEntity.getAlias());
        installation.setCategories(installationEntity.getCategories());
        installation.setDeviceType(installationEntity.getDeviceType());
        installation.setOperatingSystem(installationEntity.getOperatingSystem());

        return installation;
    }

    public static InstallationEntity toEntity(Installation installation) {
        InstallationEntity installationEntity = new InstallationEntity();
        installationEntity.setId(installation.getId());
        installationEntity.setDeviceToken(installation.getDeviceToken());
        installationEntity.setPlatform(installation.getPlatform());
        installationEntity.setOsVersion(installation.getOsVersion());
        installationEntity.setSimplePushEndpoint(installation.getSimplePushEndpoint());
        installationEntity.setEnabled(installation.isEnabled());
        installationEntity.setAlias(installation.getAlias());
        installationEntity.setCategories(installation.getCategories());
        installationEntity.setDeviceType(installation.getDeviceType());
        installationEntity.setOperatingSystem(installation.getOperatingSystem());
        installationEntity.setVariantType(installation.getVariantType());

        return installationEntity;
    }


    public static List<Installation> fromInstallationEntityCollection(List<InstallationEntity> installationEntities) {
        final List<Installation> installations = new ArrayList<Installation>();

        if (! installationEntities.isEmpty()) {
            for (InstallationEntity installationEntity : installationEntities) {
                // copy all the things and stash it:
                Installation installation = fromEntity(installationEntity);
                installations.add(installation);
            }
        }
        return installations;
    }

    public static List<PushApplication> fromPushApplicationEntityCollection(List<PushApplicationEntity> pushApplicationEntities) {
        final List<PushApplication> pushApplications = new ArrayList<PushApplication>();

        if (! pushApplicationEntities.isEmpty() ) {
            for (PushApplicationEntity pushApplicationEntity : pushApplicationEntities) {
                PushApplication pushApplication = fromEntity(pushApplicationEntity);
                pushApplications.add(pushApplication);
            }
        }
        return pushApplications;
    }

    public static PushApplicationEntity toEntity(PushApplication pushApplication) {
        PushApplicationEntity pushApplicationEntity = new PushApplicationEntity();
        pushApplicationEntity.setId(pushApplication.getId());
        pushApplicationEntity.setName(pushApplication.getName());
        pushApplicationEntity.setDeveloper(pushApplication.getDeveloper());
        pushApplicationEntity.setDescription(pushApplication.getDescription());
        pushApplicationEntity.setPushApplicationID(pushApplication.getPushApplicationID());
        pushApplicationEntity.setMasterSecret(pushApplication.getMasterSecret());

        // all the variants...
        pushApplicationEntity.setAndroidVariants((Set<AndroidVariantEntity>) copyEntities(pushApplication.getAndroidVariants()));
        pushApplicationEntity.setIOSVariants((Set<iOSVariantEntity>) copyEntities(pushApplication.getIOSVariants()));
        pushApplicationEntity.setChromePackagedAppVariants((Set<ChromePackagedAppVariantEntity>) copyEntities(pushApplication.getChromePackagedAppVariants()));
        pushApplicationEntity.setSimplePushVariants((Set<SimplePushVariantEntity>) copyEntities(pushApplication.getSimplePushVariants()));

        return pushApplicationEntity;
    }

    public static PushApplication fromEntity(PushApplicationEntity pushApplicationEntity) {
        if (pushApplicationEntity == null) {
            return null;
        }
        PushApplication pushApplication = new PushApplication();
        pushApplication.setId(pushApplicationEntity.getId());
        pushApplication.setName(pushApplicationEntity.getName());
        pushApplication.setDeveloper(pushApplicationEntity.getDeveloper());
        pushApplication.setDescription(pushApplicationEntity.getDescription());
        pushApplication.setPushApplicationID(pushApplicationEntity.getPushApplicationID());
        pushApplication.setMasterSecret(pushApplicationEntity.getMasterSecret());

        // all the variants...
        pushApplication.setAndroidVariants((Set<AndroidVariant>) copyVariants(pushApplicationEntity.getAndroidVariants()));
        pushApplication.setIOSVariants((Set<iOSVariant>) copyVariants(pushApplicationEntity.getIOSVariants()));
        pushApplication.setChromePackagedAppVariants((Set<ChromePackagedAppVariant>) copyVariants(pushApplicationEntity.getChromePackagedAppVariants()));
        pushApplication.setSimplePushVariants((Set<SimplePushVariant>) copyVariants(pushApplicationEntity.getSimplePushVariants()));

        return pushApplication;
    }


    private static Set<? extends Variant> copyVariants(Set<? extends AbstractVariantEntity> entities) {
        final Set<Variant> variants = new HashSet<Variant>();

        for (AbstractVariantEntity entity : entities) {
            // copy and stash:
            Variant variant = fromEntity(entity);
            variants.add(variant);
        }
        return variants;
    }

    private static Set<? extends AbstractVariantEntity> copyEntities(Set<? extends Variant> variants) {
        final Set<AbstractVariantEntity> entities = new HashSet<AbstractVariantEntity>();

        for (Variant variant : variants) {
            AbstractVariantEntity entity = toEntity(variant);
            entities.add(entity);
        }
        return entities;
    }


}
