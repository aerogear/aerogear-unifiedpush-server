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
package org.jboss.aerogear.unifiedpush.keycloak;

import org.jboss.resteasy.core.Dispatcher;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.provider.ProviderSession;
import org.keycloak.services.managers.RealmManager;
import org.keycloak.services.resources.KeycloakApplication;

import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class UpsKeycloakApplication extends KeycloakApplication {
    public UpsKeycloakApplication(@Context ServletContext context, @Context Dispatcher dispatcher) {
        super(context, dispatcher);
    }

    @Override
    protected void setupDefaultRealm(String contextPath) {
        super.setupDefaultRealm(contextPath);

        ProviderSession providerSession = providerSessionFactory.createSession();
        KeycloakSession session = providerSession.getProvider(KeycloakSession.class);
        session.getTransaction().begin();

        // disable master realm by deleting the admin user.
        try {
            RealmManager manager = new RealmManager(session);
            RealmModel master = manager.getKeycloakAdminstrationRealm();
            UserModel admin = master.getUser("admin");
            if (admin != null) master.removeUser(admin.getLoginName());
            session.getTransaction().commit();
        } finally {
            providerSession.close();
        }

    }
}