package org.jboss.aerogear.unifiedpush.message.util.webpush.encryption;

import org.apache.commons.codec.DecoderException;
import org.jboss.aerogear.unifiedpush.dto.WebPushToken;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.interfaces.ECPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

import static org.jboss.aerogear.unifiedpush.message.util.webpush.encryption.Constants.AESGCM128;
import static org.jboss.aerogear.unifiedpush.message.util.webpush.encryption.Constants.NONCE;

public final class WebPushEncryptionUtil {

    private WebPushEncryptionUtil() {}

    public static WebPushEncryptedData generateEncryptedPayload(final WebPushToken webPushToken, final String payload)
            throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException,
            InvalidKeySpecException, InvalidKeyException, IOException, NoSuchPaddingException, BadPaddingException,
            IllegalBlockSizeException, DecoderException {

        EllipticCurveKeyUtil ellipticCurveKeyUtil = new EllipticCurveKeyUtil();

        KeyPair serverKeys = ellipticCurveKeyUtil.generateServerKeyPair();

        final ECPublicKey clientPublicKey = ellipticCurveKeyUtil.loadP256Dh(webPushToken.getPublicKey());
        final byte[] clientAuth = Base64.getUrlDecoder().decode(webPushToken.getAuthSercret());
        final byte[] salt = PushApiUtil.generateSalt();
        final byte[] sharedSecret = ellipticCurveKeyUtil.generateSharedSecret(serverKeys, clientPublicKey);
        final byte[] serverPublicKeyBytes = ellipticCurveKeyUtil.publicKeyToBytes((ECPublicKey) serverKeys.getPublic());
        final byte[] clientPublicKeyBytes = ellipticCurveKeyUtil.publicKeyToBytes(clientPublicKey);
        final byte[] nonceInfo = PushApiUtil.generateInfo(serverPublicKeyBytes, clientPublicKeyBytes, NONCE);
        final byte[] contentEncryptionKeyInfo = PushApiUtil.generateInfo(serverPublicKeyBytes, clientPublicKeyBytes, AESGCM128);

        byte[] cipherText = PushApiUtil.encryptPayload(payload, sharedSecret, salt, contentEncryptionKeyInfo, nonceInfo, clientAuth);

        return new WebPushEncryptedData(cipherText, salt, serverPublicKeyBytes);
    }
}
