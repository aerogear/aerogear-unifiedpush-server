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
package org.jboss.aerogear.unifiedpush.migrator;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import com.mysql.management.MysqldResource;
import com.mysql.management.MysqldResourceI;

public class EmbeddedMysqlDatabase {

    private MysqldResource mysqldResource;
    private String url;

    public void start(String databaseName) {
        String tmpDir = System.getProperty("java.io.tmpdir");
        File baseDir = new File(tmpDir, databaseName);
        FileUtils.deleteQuietly(baseDir);
        int port = getFreePort();

        Map<String, String> databaseOptions = new HashMap<>();
        databaseOptions.put(MysqldResourceI.PORT, Integer.toString(port));

        mysqldResource = new MysqldResource(baseDir);
        mysqldResource.start("mysql", databaseOptions);

        url = "jdbc:mysql://localhost:" + port + "/" + databaseName + "?" + "createDatabaseIfNotExist=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";
    }

    public void close() {
        if (mysqldResource != null) {
            mysqldResource.shutdown();
            FileUtils.deleteQuietly(mysqldResource.getBaseDir());
        }
    }

    public String getUrl() {
        return url;
    }

    private int getFreePort() {
        ServerSocket socket = null;
        try {
            socket = new ServerSocket(0);
            return socket.getLocalPort();
        } catch (IOException e) {
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                }
            }
        }
        return -1;
    }

}