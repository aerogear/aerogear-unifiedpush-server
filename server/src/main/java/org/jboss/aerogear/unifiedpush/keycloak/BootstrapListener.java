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

import org.keycloak.adapters.AdapterDeploymentContext;
import org.keycloak.representations.adapters.config.AdapterConfig;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class BootstrapListener implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        AdapterDeploymentContext deploymentContext = (AdapterDeploymentContext)sce.getServletContext().getAttribute(AdapterDeploymentContext.class.getName());
        AdapterConfig config = new AdapterConfig();
        config.setRealm("aerogear");
        config.setResource("unified-push-server");
        config.setAuthServerUrl("/auth");
        config.setSslNotRequired(true);
        //config.setBearerOnly(true);
        config.setDisableTrustManager(true);
        deploymentContext.updateDeployment(config);

    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {

    }
}
