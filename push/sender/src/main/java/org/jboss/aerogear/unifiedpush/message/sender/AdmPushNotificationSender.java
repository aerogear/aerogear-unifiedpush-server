package org.jboss.aerogear.unifiedpush.message.sender;


import org.jboss.aerogear.unifiedpush.api.AdmVariant;
import org.jboss.aerogear.unifiedpush.api.Variant;

import org.jboss.aerogear.unifiedpush.message.UnifiedPushMessage;
import org.jboss.aerogear.unifiedpush.utils.AeroGearLogger;
import org.jboss.aerogear.adm.ADM;
import org.jboss.aerogear.adm.AdmService;
import org.jboss.aerogear.adm.PayloadBuilder;

import java.util.Collection;
import java.util.Set;

@SenderType(AdmVariant.class)
public class AdmPushNotificationSender implements PushNotificationSender {
    private final AeroGearLogger logger = AeroGearLogger.getInstance(AdmPushNotificationSender.class);

    @Override
    public void sendPushMessage(Variant variant, Collection<String> clientIdentifiers, UnifiedPushMessage pushMessage, NotificationSenderCallback senderCallback) {
        AdmService admService = ADM.newService();

        PayloadBuilder builder = ADM.newPayload();

        //flatten the "special keys"
        builder.dataField("alert", pushMessage.getMessage().getAlert());
        builder.dataField("sound", pushMessage.getMessage().getSound());
        builder.dataField("badge", "" + pushMessage.getMessage().getBadge());

        // if present, apply the time-to-live metadata:
        int ttl = pushMessage.getConfig().getTimeToLive();
        if (ttl != -1) {
            builder.expiresAfter(ttl);
        }

        //dirty hack for cordova,
        //TODO should be removed once we have our clients SDKs
        builder.dataField("message","useless payload");

        //Handle consolidation key
        builder.consolidationKey("my other app");

        // iterate over the missing keys:
        Set<String> keys = pushMessage.getMessage().getUserData().keySet();
        for (String key : keys) {
            builder.dataField(key, pushMessage.getMessage().getUserData().get(key));
        }
        System.out.println(builder.build());


        final AdmVariant admVariant = (AdmVariant) variant;
        for(String token : clientIdentifiers) {
            try {
                admService.sendMessageToDevice(token, admVariant.getClientId(), admVariant.getClientSecret(),  builder.build());
                senderCallback.onSuccess();
            } catch (Exception e) {
                logger.severe("Error sending payload to ADM server", e);
                senderCallback.onError(e.getMessage());
            }
        }
    }
}
