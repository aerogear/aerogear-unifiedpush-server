package org.jboss.aerogear.unifiedpush.message.webpush;

import com.google.gson.Gson;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.jboss.aerogear.unifiedpush.api.FlatPushMessageInformation;
import org.jboss.aerogear.unifiedpush.api.Variant;
import org.jboss.aerogear.unifiedpush.api.VariantType;
import org.jboss.aerogear.unifiedpush.api.WebPushRegistration;
import org.jboss.aerogear.unifiedpush.api.WebPushVariant;
import org.jboss.aerogear.unifiedpush.dao.FlatPushMessageInformationDao;
import org.jboss.aerogear.unifiedpush.message.NotificationDispatcher;
import org.jboss.aerogear.unifiedpush.message.UnifiedPushMessage;
import org.jboss.aerogear.unifiedpush.message.holder.MessageHolderWithTokens;
import org.jboss.aerogear.unifiedpush.message.sender.NotificationSenderCallback;
import org.jboss.aerogear.unifiedpush.message.sender.PushNotificationSender;
import org.jboss.aerogear.unifiedpush.message.sender.SenderType;
import org.jboss.aerogear.unifiedpush.service.ClientInstallationService;
import org.jose4j.lang.JoseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Base64;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.apache.http.HttpStatus.SC_GONE;
import static org.apache.http.HttpStatus.SC_NOT_FOUND;
import static org.apache.http.HttpStatus.SC_REQUEST_TOO_LONG;
import static org.jboss.aerogear.unifiedpush.utils.KeyUtils.getUserPublicKey;
import static org.jboss.aerogear.unifiedpush.utils.KeyUtils.loadPrivateKey;
import static org.jboss.aerogear.unifiedpush.utils.KeyUtils.loadPublicKey;


@Stateless
@SenderType(VariantType.WEB_PUSH)
/**
 * This class sends web push messages.
 */
public class WebPushSender implements PushNotificationSender {

    private static final Logger logger = LoggerFactory.getLogger(WebPushSender.class);
    private Gson gson = new Gson();

    @Inject
    private ClientInstallationService clientInstallationService;

    @Inject
    private NotificationDispatcher dispatcher;

    @Inject
    private FlatPushMessageInformationDao flatPushMessageInformationDao;

    /**
     * Default for CDI
     */
    public WebPushSender() {
    }

    /**
     * This is a constructor for injecting dependencies for testing.
     * @param flatPushMessageInformationDao
     */
    public WebPushSender(FlatPushMessageInformationDao flatPushMessageInformationDao, NotificationDispatcher dispatcher, ClientInstallationService clientInstallationService) {
        this.flatPushMessageInformationDao = flatPushMessageInformationDao;
        this.dispatcher = dispatcher;
        this.clientInstallationService = clientInstallationService;
    }

    @Override
    public void sendPushMessage(final Variant variant, final Collection<String> tokens, final UnifiedPushMessage pushMessage, final String pushMessageInformationId, final NotificationSenderCallback senderCallback) {
        final WebPushVariant webPushVariant = (WebPushVariant) variant;
        final String privateKey = webPushVariant.getPrivateKey();
        final String publicKey = webPushVariant.getPublicKey();
        final String alias = webPushVariant.getAlias();
        final PushService webPushService = new PushService();
        try {
            webPushService.setPrivateKey(loadPrivateKey(privateKey));
            webPushService.setPublicKey(loadPublicKey(publicKey));
            webPushService.setSubject(alias);

            // storage for all the invalid registration IDs:
            final Set<String> inactiveTokens = new HashSet<>();

            // storage for all the invalid registration IDs:
            final Set<String> rescheduleTokens = new HashSet<>();

            tokens.forEach(token -> {
                String tokenAsJson = new String(Base64.getDecoder().decode(token));
                final WebPushRegistration registration = gson.fromJson(tokenAsJson, WebPushRegistration.class);

                try {
                    final Notification notification = new Notification(registration.getEndpoint(), getUserPublicKey(registration),
                            registration.getAuthAsBytes(), gson.toJson(pushMessage.getMessage()).getBytes("UTF-8"));

                    final HttpResponse response = webPushService.send(notification);
                    final int responseCode = response.getStatusLine().getStatusCode();
                    final String responseReason = response.getStatusLine().getReasonPhrase();
                    switch (response.getStatusLine().getStatusCode()) {
                        case HttpStatus.SC_CREATED:
                            break;
                        case 429:
                            //reschedule
                            rescheduleTokens.add(token);
                            break;
                        case SC_NOT_FOUND:
                            //not breaking here is intentional
                        case SC_GONE:
                            inactiveTokens.add(token);
                            break;
                        case SC_REQUEST_TOO_LONG:
                            final String tooLongMessage = String.format("Request was too long. Message id %s", pushMessageInformationId);
                            logger.error(tooLongMessage);
                            senderCallback.onError(tooLongMessage);
                            break;
                        case SC_BAD_REQUEST:
                            final String badRequestMessage = String.format("Bad request. Message id %s", pushMessageInformationId);
                            logger.error(badRequestMessage);
                            senderCallback.onError(badRequestMessage);
                            break;
                        default:
                            final String unhandledMessage = String.format("Unknown message response. Was %d with http message %s. Message id %s", responseCode, responseReason, pushMessageInformationId);
                            logger.error(unhandledMessage);
                            senderCallback.onError(unhandledMessage);
                            break;


                    }
                } catch (GeneralSecurityException | IOException | JoseException | ExecutionException | InterruptedException e) {
                    logger.error("Error sending web push message.", e);
                    senderCallback.onError(e.getMessage());
                }

            });

            senderCallback.onSuccess();

            if (! inactiveTokens.isEmpty()) {
                // trigger asynchronous deletion:
                logger.info(String.format("Based on FCM response data and error codes, deleting %d invalid or duplicated Android installations", inactiveTokens.size()));
                clientInstallationService.removeInstallationsForVariantByDeviceTokens(variant.getVariantID(), inactiveTokens);
            }

            if (! rescheduleTokens.isEmpty()) {
                final FlatPushMessageInformation flatPushMessageInformation = removeErrors(flatPushMessageInformationDao.find(pushMessageInformationId));
                MessageHolderWithTokens newMessage = new MessageHolderWithTokens(flatPushMessageInformation, pushMessage, variant, rescheduleTokens, 0);
                newMessage.incrRetryCount();
                dispatcher.sendMessagesToPushNetwork(newMessage);
            }

        } catch (GeneralSecurityException e) {
            logger.error("Could not load VAPID keys.", e);
            senderCallback.onError(e.getMessage());
        }

    }

    private FlatPushMessageInformation removeErrors(FlatPushMessageInformation pushMessageInformation) {
        FlatPushMessageInformation info = new FlatPushMessageInformation();
        info.setAppOpenCounter(pushMessageInformation.getAppOpenCounter());
        info.setClientIdentifier(pushMessageInformation.getClientIdentifier());
        info.setFirstOpenDate(pushMessageInformation.getFirstOpenDate());
        info.setId(pushMessageInformation.getId());
        info.setIpAddress(pushMessageInformation.getIpAddress());
        info.setLastOpenDate(pushMessageInformation.getLastOpenDate());
        info.setPushApplicationId(pushMessageInformation.getPushApplicationId());
        info.setRawJsonMessage(pushMessageInformation.getRawJsonMessage());
        info.setSubmitDate(pushMessageInformation.getSubmitDate());
        return info;
    }
}
