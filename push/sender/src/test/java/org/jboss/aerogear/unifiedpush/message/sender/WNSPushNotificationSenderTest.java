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
    private WNSPushNotificationSender sender = new WNSPushNotificationSender();


    @Test
    public void shouldEncodeUserDataInLaunchParam() {
        //given
        UnifiedPushMessage pushMessage = getUnifiedPushMessage();

        //when
        final WnsToast toast = sender.createToastMessage(pushMessage);

        //then
        assertThat(toast.launch).isEqualTo("/Root.xaml" + QUERY);
    }

    @Test
    public void shouldEncodeUserDataInCordovaPage() {
        //given
        UnifiedPushMessage pushMessage = getUnifiedPushMessage();
        pushMessage.getMessage().setPage("cordova");

        //when
        final WnsToast toast = sender.createToastMessage(pushMessage);

        //then
        assertThat(toast.launch).isEqualTo(WNSPushNotificationSender.CORDOVA_PAGE + QUERY);
    }

    @Test
    public void shouldNoPage() {
        //given
        UnifiedPushMessage pushMessage = getUnifiedPushMessage();
        pushMessage.getMessage().setPage(null);

        //when
        final WnsToast toast = sender.createToastMessage(pushMessage);

        //then
        assertThat(toast.launch).isNull();
    }

    private UnifiedPushMessage getUnifiedPushMessage() {
        UnifiedPushMessage message = new UnifiedPushMessage();
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("key", "value");
        data.put("ke2", "value2");
        message.getMessage().setPage("/Root.xaml");
        message.getMessage().setUserData(data);
        return message;
    }
}
