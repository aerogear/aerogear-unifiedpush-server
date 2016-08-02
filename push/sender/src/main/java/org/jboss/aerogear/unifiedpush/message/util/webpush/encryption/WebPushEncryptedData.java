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

import java.io.Serializable;
import java.util.Arrays;

public class WebPushEncryptedData implements Serializable {

    private static final long serialVersionUID = -1654509737937585694L;

    private final byte[] ciphertext;
    private final byte[] salt;
    private final byte[] dh;

    public WebPushEncryptedData(byte[] ciphertext, byte[] salt, byte[] dh) {
        this.ciphertext = ciphertext;
        this.salt = salt;
        this.dh = dh;
    }

    public byte[] getCiphertext() {
        return ciphertext;
    }

    public byte[] getSalt() {
        return salt;
    }

    public byte[] getDh() {
        return dh;
    }

    @Override
    public String toString() {
        return "WebPushEncryptedData{" +
                "ciphertext=" + Arrays.toString(ciphertext) +
                ", salt=" + Arrays.toString(salt) +
                ", dh=" + Arrays.toString(dh) +
                '}';
    }
}
