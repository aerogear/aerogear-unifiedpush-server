package org.jboss.aerogear.unifiedpush.service.impl;

import org.keycloak.admin.client.resource.RealmResource;

public class KeycloakRealmWrapper {

    private RealmResource realm;

    public KeycloakRealmWrapper(RealmResource realm) {
        this.realm = realm;
    }

    public RealmResource getRealm() {
        return realm;
    }

}
