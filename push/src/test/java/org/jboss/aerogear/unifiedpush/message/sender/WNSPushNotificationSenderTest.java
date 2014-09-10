package org.jboss.aerogear.unifiedpush.message.sender;

import ar.com.fernandospr.wns.model.WnsToast;
import org.jboss.aerogear.unifiedpush.message.UnifiedPushMessage;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 */
public class WNSPushNotificationSenderTest {

    private static final String QUERY = "?ke2=value2&key=value";

    @Test
    public void shouldEncodeUserDataInLaunchParam() {
        //given
        WNSPushNotificationSender sender = new WNSPushNotificationSender();
        UnifiedPushMessage pushMessage = getUnifiedPushMessage();

        //when
        final WnsToast toast = sender.createToastMessage(pushMessage);

        //then
        assertThat(toast.launch).isEqualTo("/Root.xaml" + QUERY);
    }

    @Test
    public void shouldEncodeUserDataInCordovaPage() {
        //given
        WNSPushNotificationSender sender = new WNSPushNotificationSender();
        UnifiedPushMessage pushMessage = getUnifiedPushMessage();
        pushMessage.getData().put("page", "cordova");

        //when
        final WnsToast toast = sender.createToastMessage(pushMessage);

        //then
        assertThat(toast.launch).isEqualTo(WNSPushNotificationSender.CORDOVA_PAGE + QUERY);
    }

    private UnifiedPushMessage getUnifiedPushMessage() {
        Map<String, Object> message = new HashMap<String, Object>();
        Map<String, String> data = new HashMap<String, String>();
        data.put("key", "value");
        data.put("ke2", "value2");
        data.put("page", "/Root.xaml");
        message.put("message", data);
        return new UnifiedPushMessage(message);
    }
}
