package org.jboss.aerogear.unifiedpush.keycloak;

import javax.servlet.ServletContextEvent;

public class KeycloakSessionDestroyListener extends org.keycloak.services.listeners.KeycloakSessionDestroyListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
    	ApplicationUtil.setServletContext(sce.getServletContext());
    }
}
