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
package org.jboss.aerogear.unifiedpush.message.sender.apns;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.x500.X500Principal;
import java.io.ByteArrayInputStream;
import java.security.KeyStore;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ApnsUtil {

    private static final String KEYSTORE_TYPE = "PKCS12";
    private static final String TOPIC_PATTERN = ".*UID=([^,]+).*";

    private static final Logger LOGGER = LoggerFactory.getLogger(ApnsUtil.class);

    private ApnsUtil() {
    }

    public static String readDefaultTopic(final byte[] keystore, final char[] password) {
        try {
            final KeyStore keyStore = KeyStore.getInstance(KEYSTORE_TYPE);
            keyStore.load(new ByteArrayInputStream(keystore), password);

            final Enumeration<String> aliases = keyStore.aliases();

            while (aliases.hasMoreElements()) {
                final String alias = aliases.nextElement();
                final X509Certificate certificate = (X509Certificate) keyStore.getCertificate(alias);
                final X500Principal subjectX500Principal = certificate.getSubjectX500Principal();

                final String subject = subjectX500Principal.getName();
                if (subject != null ) {

                    final Pattern pattern = Pattern.compile(TOPIC_PATTERN);
                    final Matcher matcher = pattern.matcher(subject);

                    if (matcher.matches()) {
                        return matcher.group(1);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error parsing .p12 file content", e);
        }

        return null; // if no topic was found we try with null
    }

    public static boolean checkValidity(final byte[] keystore, final char[] password) {
        try {
            final KeyStore keyStore = KeyStore.getInstance(KEYSTORE_TYPE);
            keyStore.load(new ByteArrayInputStream(keystore), password);

            final Enumeration<String> aliases = keyStore.aliases();

            while (aliases.hasMoreElements()) {
                final String alias = aliases.nextElement();
                final X509Certificate certificate = (X509Certificate) keyStore.getCertificate(alias);

                try{
                    certificate.checkValidity();
                } catch (CertificateExpiredException | CertificateNotYetValidException e) {
                    LOGGER.error("Provided APNs .p12 file is expired or not yet valid");
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            LOGGER.error("Error parsing .p12 file content", e);
            return false; // garbage is also not valid
        }
    }
}