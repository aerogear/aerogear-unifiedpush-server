package org.jboss.aerogear.unifiedpush.message.jms;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.wildfly.extras.creaper.commands.foundation.online.CliFile;
import org.wildfly.extras.creaper.core.ManagementClient;
import org.wildfly.extras.creaper.core.online.ManagementProtocol;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.OnlineOptions;

/**
 * Performs the jboss-cli setup as it should be performed during installation
 */
public class MessagingSetup {

    private static final int HTTP_INTERFACE_PORT = 8080;
    private static final int HTTP_REMOTING_MANAGEMENT_PORT = 9990;

    private int managementPort;

    public MessagingSetup(URL testContextPath) {
        int portOffset = testContextPath.getPort() - HTTP_INTERFACE_PORT;
        managementPort =  portOffset + HTTP_REMOTING_MANAGEMENT_PORT;
    }

    public void install() {
        try {
            client().apply(new CliFile(new File("../../configuration/jms-setup-wildfly.cli")));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public void uninstall() {
        try {
            client().apply(new CliFile(new File("src/test/resources/jms-cleanup-wildfly.cli")));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private OnlineManagementClient client() throws IOException {
        return ManagementClient.online(OnlineOptions
                .standalone()
                .hostAndPort("localhost", managementPort)
                .protocol(ManagementProtocol.HTTP_REMOTE)
                .build()
        );
    }
}
