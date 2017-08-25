/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.unifiedpush.test.configure;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.jboss.arquillian.container.spi.context.annotation.ContainerScoped;
import org.jboss.arquillian.container.spi.event.container.AfterStart;
import org.jboss.arquillian.container.spi.event.container.BeforeStop;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.wildfly.extras.creaper.commands.foundation.online.CliFile;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.ManagementClient;
import org.wildfly.extras.creaper.core.online.ManagementProtocol;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.OnlineOptions;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

/**
 * Sets messaging up once server is started, tears it down once server stops
 */
public class MessagingSetup {

    @Inject
    @ContainerScoped
    private InstanceProducer<OnlineManagementClient> managementClient;

    private static int portOffset = 14311;

    public void setupMessaging(@Observes AfterStart event) throws IOException, CommandFailedException, InterruptedException, TimeoutException {
        final OnlineManagementClient client = createClient();
        managementClient.set(client);
        client.apply(new CliFile(new File("../configuration/jms-setup-wildfly.cli")));

        Administration administration = new Administration(client);
        administration.reloadIfRequired();
    }

    public void teardownMessaging(@Observes BeforeStop event) throws CommandFailedException, IOException, InterruptedException, TimeoutException {
        final OnlineManagementClient client = managementClient.get();
        client.apply(new CliFile(new File("src/test/resources/jms-cleanup-wildfly.cli")));

        Administration administration = new Administration(client);
        administration.reloadIfRequired();

        client.close();
    }

    private OnlineManagementClient createClient() throws IOException {
        return ManagementClient.online(OnlineOptions
                .standalone()
                .hostAndPort("localhost", portOffset++)
                .protocol(ManagementProtocol.HTTP_REMOTING)
                .build()
        );
    }

}
