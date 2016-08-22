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
package org.jboss.aerogear.unifiedpush.message.util.webpush.encryption;

import java.nio.charset.StandardCharsets;

final class Constants {

    private Constants() {}

    public static final byte[] CONTENT_ENCODING = "Content-Encoding: ".getBytes(StandardCharsets.UTF_8);
    public static final byte[] AESGCM128 = "aesgcm".getBytes(StandardCharsets.UTF_8);
    public static final byte[] NONCE = "nonce".getBytes(StandardCharsets.UTF_8);
    public static final byte[] P256 = "P-256".getBytes(StandardCharsets.UTF_8);
    public static final int GCM_TAG_LENGTH = 16; // in bytes
    public static final String SECP256R1 = "secp256r1"; // TODO change to 'prime256v1'
    public static final String HMAC_SHA256 = "HmacSHA256";
    public static final byte NULL_BYTE = 0;
    public static final byte KEY_LENGTH_BYTE = 65; // This is always 65 for our curve
    public static final int MAX_PAYLOAD_LENGTH = 4078;
}
