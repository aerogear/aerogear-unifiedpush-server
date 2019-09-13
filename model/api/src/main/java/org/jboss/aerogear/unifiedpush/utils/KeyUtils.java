package org.jboss.aerogear.unifiedpush.utils;

import nl.martijndwars.webpush.Base64Encoder;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.jce.spec.ECPrivateKeySpec;
import org.bouncycastle.jce.spec.ECPublicKeySpec;
import org.bouncycastle.math.ec.ECCurve;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.util.BigIntegers;

import java.math.BigInteger;

import java.security.*;
import java.security.spec.InvalidKeySpecException;


/**
 * This class includes convenient wrappers for bouncy castle to perform key pair validation for WebPush VAPID keys.
 *
 * This is mostly based on nl.martijndwars.webpush.Utils, but uses an explicit java.security.provider
 * instead of a lookup.
 *
 */
public class KeyUtils {

    private static final String CURVE = "prime256v1";
    private static final String ALGORITHM = "ECDH";
    private static final Provider PROVIDER = new BouncyCastleProvider();

    public static PrivateKey loadPrivateKey(String privateKey) throws  NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] decodedPrivateKey = Base64Encoder.decode(privateKey);
        BigInteger s = BigIntegers.fromUnsignedByteArray(decodedPrivateKey);
        ECParameterSpec parameterSpec = ECNamedCurveTable.getParameterSpec(CURVE);
        ECPrivateKeySpec privateKeySpec = new ECPrivateKeySpec(s, parameterSpec);
        KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM, PROVIDER);

        return keyFactory.generatePrivate(privateKeySpec);
    }

    public static PublicKey loadPublicKey(String publicKey) throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] decodedPublicKey = Base64Encoder.decode(publicKey);
        KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM, PROVIDER);
        ECParameterSpec parameterSpec = ECNamedCurveTable.getParameterSpec(CURVE);
        ECCurve curve = parameterSpec.getCurve();
        ECPoint point = curve.decodePoint(decodedPublicKey);
        ECPublicKeySpec pubSpec = new ECPublicKeySpec(point, parameterSpec);

        return keyFactory.generatePublic(pubSpec);

    }
}
