package org.jboss.aerogear.unifiedpush.message.webpush;

import com.google.gson.Gson;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import org.jboss.aerogear.unifiedpush.api.Variant;
import org.jboss.aerogear.unifiedpush.api.VariantType;
import org.jboss.aerogear.unifiedpush.api.WebPushRegistration;
import org.jboss.aerogear.unifiedpush.api.WebPushVariant;
import org.jboss.aerogear.unifiedpush.message.UnifiedPushMessage;
import org.jboss.aerogear.unifiedpush.message.sender.NotificationSenderCallback;
import org.jboss.aerogear.unifiedpush.message.sender.PushNotificationSender;
import org.jboss.aerogear.unifiedpush.message.sender.SenderType;
import org.jose4j.lang.JoseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Stateless;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collection;
import java.util.concurrent.ExecutionException;

import static org.jboss.aerogear.unifiedpush.utils.KeyUtils.getUserPublicKey;
import static org.jboss.aerogear.unifiedpush.utils.KeyUtils.loadPrivateKey;
import static org.jboss.aerogear.unifiedpush.utils.KeyUtils.loadPublicKey;


@Stateless
@SenderType(VariantType.WEB_PUSH)
public class WebPushSender implements PushNotificationSender {

    private static final Logger logger = LoggerFactory.getLogger(WebPushSender.class);
    private Gson gson = new Gson();


    @Override
    public void sendPushMessage(Variant variant, Collection<String> tokens, UnifiedPushMessage pushMessage, String pushMessageInformationId, NotificationSenderCallback senderCallback) {
        WebPushVariant webPushVariant = (WebPushVariant) variant;
        String privateKey = webPushVariant.getPrivateKey();
        String publicKey = webPushVariant.getPublicKey();
        String alias = webPushVariant.getAlias();
        PushService webPushService = new PushService();
        try {
            webPushService.setPrivateKey(loadPrivateKey(privateKey));
            webPushService.setPublicKey(loadPublicKey(publicKey));
            webPushService.setSubject(alias);

            tokens.forEach(token -> {
                WebPushRegistration registration = gson.fromJson(token, WebPushRegistration.class);
                Notification notification = null;
                try {
                    notification = new Notification(registration.getEndpoint(), getUserPublicKey(registration),
                            registration.getAuthAsBytes(), pushMessage.getMessage().getAlert().getBytes());
                    webPushService.send(notification);
                } catch (GeneralSecurityException | IOException | JoseException | ExecutionException | InterruptedException e) {
                    logger.error("Error sending web push message.", e);
                    senderCallback.onError(e.getMessage());
                }

            });

        } catch (GeneralSecurityException e) {
            logger.error("Could not load VAPID keys.", e);
            senderCallback.onError(e.getMessage());
        }

    }
}
