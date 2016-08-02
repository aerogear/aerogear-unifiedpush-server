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

import org.apache.commons.codec.DecoderException;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.ECPointUtil;
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;
import org.bouncycastle.jce.spec.ECNamedCurveSpec;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.jce.spec.ECPrivateKeySpec;

import javax.crypto.KeyAgreement;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECPoint;
import java.security.spec.ECPublicKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

import static org.apache.commons.codec.binary.Hex.decodeHex;

class EllipticCurveKeyUtil {

    public static final String CRYPTO_TYPE_ECDH = "ECDH";
    public static final String PROVIDER_BOUNCY_CASTLE = "BC";
    private final KeyFactory _keyFactory;
    private final ECNamedCurveParameterSpec _ecNamedCurveParameterSpec;
    private final ECNamedCurveSpec _ecNamedCurveSpec;
    private final ECParameterSpec _ecParameterSpec;
    private final KeyPairGenerator _keyPairGenerator;

    public EllipticCurveKeyUtil()
            throws NoSuchProviderException, NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

        _keyFactory = KeyFactory.getInstance(CRYPTO_TYPE_ECDH, PROVIDER_BOUNCY_CASTLE);
        _ecNamedCurveParameterSpec = ECNamedCurveTable.getParameterSpec(Constants.SECP256R1); // P256 curve
        _ecNamedCurveSpec = new ECNamedCurveSpec(Constants.SECP256R1, _ecNamedCurveParameterSpec.getCurve(), _ecNamedCurveParameterSpec
                .getG(), _ecNamedCurveParameterSpec.getN());

        _ecParameterSpec = ECNamedCurveTable.getParameterSpec(Constants.SECP256R1); // P256 curve
        _keyPairGenerator = KeyPairGenerator.getInstance(CRYPTO_TYPE_ECDH, PROVIDER_BOUNCY_CASTLE);
        _keyPairGenerator.initialize(_ecParameterSpec, new SecureRandom());
    }

    /**
     * Creates a ECDH keypair from the curve parameters for the p256 curve
     * @param x Affine X for the public key point
     * @param y Affine Y for the public key point
     * @param s S parameter for the private key
     * @return ECDH keypair
     * @throws NoSuchProviderException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     */
    public KeyPair loadECKeyPair(BigInteger x, BigInteger y, BigInteger s)
            throws NoSuchProviderException, NoSuchAlgorithmException, InvalidKeySpecException {
        ECPoint ecPoint = new ECPoint(x, y);
        ECPublicKeySpec pubKeySpec = new ECPublicKeySpec(ecPoint, _ecNamedCurveSpec);
        ECPrivateKeySpec privateKeySpec = new ECPrivateKeySpec(s, _ecNamedCurveParameterSpec);
        ECPublicKey publicKey =  (ECPublicKey) _keyFactory.generatePublic(pubKeySpec);
        ECPrivateKey privateKey = (ECPrivateKey) _keyFactory.generatePrivate(privateKeySpec);
        return new KeyPair(publicKey, privateKey);
    }

    /**
     * Generates a new keypair for ECDH using the p256 curve.
     * Creating a new keypair per request is expensive, but preferred
     * @return ECDH Keypair
     * @throws NoSuchAlgorithmException
     * @throws InvalidAlgorithmParameterException
     * @throws NoSuchProviderException
     */
    public KeyPair generateServerKeyPair()
            throws NoSuchAlgorithmException, InvalidAlgorithmParameterException, NoSuchProviderException {

        return _keyPairGenerator.generateKeyPair();
    }

    /**
     * Converts the ECDH public key to bytes (has to be 65 bytes in length)
     * @param publicKey Public key for ECDH p256 curve
     * @return bytearray representation of key
     */
    public byte[] publicKeyToBytes(final ECPublicKey publicKey) throws DecoderException {
        ECPoint point = publicKey.getW();
        String x = point.getAffineX().toString(16);
        String y = point.getAffineY().toString(16);

    /*
     *  Format is 04 followed by 32 bytes (64 hex) each for the X,Y coordinates
    */
        StringBuilder sb = new StringBuilder();
        sb.append("04");

        for (int i = 0; i < 64 - x.length(); i++) {
            sb.append(0);
        }
        sb.append(x);

        for (int i = 0; i < 64 - y.length(); i++) {
            sb.append(0);
        }
        sb.append(y);
        return decodeHex(sb.toString().toCharArray());
    }

    /**
     * Creates a public key from the p256dh encoded using URL-safe Base64
     * @param p256dh p256dh string
     * @return Public Key
     * @throws NoSuchProviderException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     */
    public ECPublicKey loadP256Dh(final String p256dh)
            throws NoSuchProviderException, NoSuchAlgorithmException, InvalidKeySpecException {
        final byte[] p256dhBytes = Base64.getUrlDecoder().decode(p256dh);
        final ECPoint point = ECPointUtil.decodePoint(_ecNamedCurveSpec.getCurve(), p256dhBytes);
        ECPublicKeySpec pubKeySpec = new ECPublicKeySpec(point, _ecNamedCurveSpec);
        return (ECPublicKey) _keyFactory.generatePublic(pubKeySpec);
    }

    /**
     * Computes the shared secret for ECDH using the server keys and the client public key
     * @param serverKeys Server keypair
     * @param clientPublicKey Client public key
     * @return p256dh shared secret
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     * @throws NoSuchProviderException
     */
    public byte[] generateSharedSecret(final KeyPair serverKeys, final PublicKey clientPublicKey)
            throws NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException {
        KeyAgreement keyAgreement = KeyAgreement.getInstance(CRYPTO_TYPE_ECDH, PROVIDER_BOUNCY_CASTLE);
        keyAgreement.init(serverKeys.getPrivate());
        keyAgreement.doPhase(clientPublicKey, true);
        return keyAgreement.generateSecret();
    }
}
