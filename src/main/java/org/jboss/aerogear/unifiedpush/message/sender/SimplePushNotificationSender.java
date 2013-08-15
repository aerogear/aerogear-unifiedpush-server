/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.unifiedpush.message.sender;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.httpclient.HttpsURL;

@ApplicationScoped
public class SimplePushNotificationSender implements Serializable {
    private static final long serialVersionUID = 5747687132270998712L;

    private static final Charset UTF_8 = Charset.forName("UTF-8");

    @Inject
    private Logger logger;

    /**
     * Sends SimplePush notifications to all connected clients, that are represented by
     *
     * @param pushEndpointURLs List of URL used for the different clients/endpoints on a SimplePush network/server.
     * the {@link Collection} of channelIDs, for the given SimplePush network.
     *
     * @param payload the payload, or version string, to be submitted
     */
    public void sendMessage(List<String> pushEndpointURLs, String payload) {
        // iterate over all the given channels, if there are channels:
        for (String clientURL : pushEndpointURLs) {

            HttpURLConnection conn = null;
            try {
                // PUT the version payload to the SimplePushServer
                logger.fine(String.format("Sending transformed SimplePush version: '%s' to %s", payload, clientURL));
                conn = put(clientURL, payload);
                int simplePushStatusCode = conn.getResponseCode();
                logger.info("SimplePush Status: " + simplePushStatusCode);

                if (200 != simplePushStatusCode) {
                    logger.severe("ERROR ??????     STATUS CODE, from PUSH NETWORK was NOT 200, but....: " + simplePushStatusCode);
                }
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Error during PUT execution to SimplePush Network", e);
            } finally {
                // tear down
                if (conn != null ) {
                    conn.disconnect();
                }
            }
        }
    }

    /**
     * Returns HttpURLConnection that 'puts' the given body to the given URL.
     */
    protected HttpURLConnection put(String url, String body) throws IOException {

        if (url == null || body == null) {
            throw new IllegalArgumentException("arguments cannot be null");
        }

        byte[] bytes = body.getBytes(UTF_8);
        HttpURLConnection conn = getConnection(url);
        conn.setDoOutput(true);
        conn.setUseCaches(false);
        conn.setFixedLengthStreamingMode(bytes.length);
        conn.setRequestProperty("Accept", "application/x-www-form-urlencoded");
        conn.setRequestMethod("PUT");
        OutputStream out = null;
        try {
            out = conn.getOutputStream();
            out.write(bytes);
        } finally {
            // in case something blows up, while writing
            // the payload, we wanna close the stream:
            if (out != null) {
                out.close();
            }
        }
        return conn;
    }

    /**
     * Convenience method to open/establish a HttpURLConnection.
     */
    protected HttpURLConnection getConnection(String url) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        if (conn instanceof HttpsURLConnection) {
            setCustomTrustStore(conn, "/openshift.truststore", "password");
        }
        return conn;
    }

    private void setCustomTrustStore(final HttpURLConnection conn, final String trustStore, final String password) throws IOException {
        try {
            final X509TrustManager customTrustManager = getCustomTrustManager(getDefaultTrustManager(), getCustomTrustStore(trustStore, password));
            setTrustStoreForConnection((HttpsURLConnection) conn, customTrustManager);
        } catch (final Exception e) {
            throw new IOException(e);
        }
    }

    private X509TrustManager getCustomTrustManager(final X509TrustManager defaultTrustManager, final KeyStore customTrustStore)
            throws NoSuchAlgorithmException, KeyStoreException {
        final TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(customTrustStore);
        final X509TrustManager customTrustManager = (X509TrustManager) tmf.getTrustManagers()[0];
        return new DelegatingTrustManager(defaultTrustManager, customTrustManager);
    }

    private X509TrustManager getDefaultTrustManager() throws NoSuchAlgorithmException, KeyStoreException {
        final TrustManagerFactory deftmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        deftmf.init((KeyStore)null);
        final TrustManager[] trustManagers = deftmf.getTrustManagers();
        for (TrustManager trustManager : trustManagers) {
            if (trustManager instanceof X509TrustManager) {
                return (X509TrustManager) trustManager;
            }
        }
        throw new RuntimeException("Could not find a default trustmanager");
    }

    private KeyStore getCustomTrustStore(final String trustStore, final String password) throws NoSuchAlgorithmException, CertificateException, IOException, KeyStoreException {
        final KeyStore customTrustStore = KeyStore.getInstance(KeyStore.getDefaultType());
        customTrustStore.load(getClass().getResourceAsStream(trustStore), password.toCharArray());
        return customTrustStore;
    }

    private void setTrustStoreForConnection(final HttpsURLConnection connection, final X509TrustManager trustManager)
            throws KeyManagementException, NoSuchAlgorithmException {
        final SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, new TrustManager[]{trustManager}, null);
        connection.setSSLSocketFactory(sslContext.getSocketFactory());
    }

    private class DelegatingTrustManager implements X509TrustManager {

        private final X509TrustManager delegate;
        private final X509TrustManager custom;

        public DelegatingTrustManager(final X509TrustManager delegate, final X509TrustManager custom) {
            this.delegate = delegate;
            this.custom = custom;
        }

        @Override
        public void checkServerTrusted(final X509Certificate[] chain, final String authType) throws CertificateException {
            try {
                custom.checkClientTrusted(chain, authType);
            } catch (final CertificateException e) {
                delegate.checkServerTrusted(chain, authType);
            }
        }

        @Override
        public void checkClientTrusted(final X509Certificate[] chain, final String authType) throws CertificateException {
            delegate.checkClientTrusted(chain, authType);
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return delegate.getAcceptedIssuers();
        }

    }
}
