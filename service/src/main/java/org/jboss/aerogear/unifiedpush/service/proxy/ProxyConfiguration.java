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
package org.jboss.aerogear.unifiedpush.service.proxy;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;


/**
 * responsible for handling authentication challenges from outbound HTTP_PROXY
 */
@Startup
@Singleton
public class ProxyConfiguration {

    @PostConstruct
    public void setupAuth(){
        Authenticator.setDefault(new Authenticator() {

            @Override protected PasswordAuthentication getPasswordAuthentication() {
                String proxyHost = System.getenv("HTTP_PROXY_HOST");
                String proxyUser = System.getenv("HTTP_PROXY_USER");
                String proxyPass = System.getenv("HTTP_PROXY_PASS");
                String hostAskingForAuth = this.getRequestingHost();
                if(hostAskingForAuth.equals(proxyHost) && (! "".equals(proxyUser) && ! "".equals(proxyPass))){
                    return new PasswordAuthentication(proxyUser,proxyPass.toCharArray());
                }
                return super.getPasswordAuthentication();
            }
        });
    }

    public static Boolean hasHttpProxyConfig(){
        String proxyHost = System.getenv("HTTP_PROXY_HOST");
        String proxyPort = System.getenv("HTTP_PROXY_PORT");
        return ((null != proxyPort && ! "".equals(proxyPort)) && (null != proxyHost && ! "".equals(proxyHost)));
    }


    public static Boolean hasBasicAuth() {
        String proxyUser = System.getenv("HTTP_PROXY_USER");
        String proxyPass = System.getenv("HTTP_PROXY_PASS");
        return ((proxyUser != null && ! "".equals(proxyUser) && proxyPass != null && ! "".equals(proxyPass)));
    }


    public static InetSocketAddress proxyAddress(){
        String proxyHost = System.getenv("HTTP_PROXY_HOST");
        String proxyPort = System.getenv("HTTP_PROXY_PORT");
        int port = Integer.parseInt(proxyPort);
        return new InetSocketAddress(proxyHost,port);
    }

    public static Boolean hasSocksProxyConfig(){
        String socksHost = System.getenv("SOCKS_PROXY_HOST");
        String socksPort = System.getenv("SOCKS_PROXY_PORT");
        return ((null != socksPort && ! "".equals(socksPort)) && (null != socksHost && ! "".equals(socksHost)));
    }

    public static InetSocketAddress socks(){
        String socksHost = System.getenv("SOCKS_PROXY_HOST");
        String socksPort = System.getenv("SOCKS_PROXY_PORT");
        int port = Integer.parseInt(socksPort);
        return new InetSocketAddress(socksHost,port);
    }

    public static String getProxyUser() {
        return System.getenv("HTTP_PROXY_USER");
    }

    public static String getProxyPass() {
        return System.getenv("HTTP_PROXY_PASS");
    }
}