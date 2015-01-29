package org.jboss.aerogear.unifiedpush.message.sender;

import ar.com.fernandospr.wns.model.WnsToast;
import org.jboss.aerogear.unifiedpush.message.Message;
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
    public void shouldWorkWithEmptyNullUserData() {
        //given
        Message message = getUnifiedPushMessage();
        message.setUserData(new HashMap<String, Object>());

        //when
        WnsToast toastMessage = sender.createToastMessage(message);

        //then
        assertThat(toastMessage.launch).isEqualTo("/Root.xaml");
    }

    @Test
    public void shouldAddAlertAutomatically() {
        //given
        Message message = getUnifiedPushMessage();
        message.setAlert("My message");

        //when
        WnsToast toastMessage = sender.createToastMessage(message);

        //then
        assertThat(toastMessage.launch).isEqualTo("/Root.xaml" + QUERY + "&message=My+message");
    }

    @Test
    public void shouldEncodeUserDataInLaunchParam() {
        //given
        Message message = getUnifiedPushMessage();

        //when
        final WnsToast toast = sender.createToastMessage(message);

        //then
        assertThat(toast.launch).isEqualTo("/Root.xaml" + QUERY);
    }

    @Test
    public void shouldEncodeUserDataInCordovaPage() {
        //given
        Message pushMessage = getUnifiedPushMessage();
        pushMessage.setPage("cordova");

        //when
        final WnsToast toast = sender.createToastMessage(pushMessage);

        //then
        assertThat(toast.launch).isEqualTo(WNSPushNotificationSender.CORDOVA_PAGE + QUERY);
    }

    @Test
    public void shouldNoPage() {
        //given
        Message message = getUnifiedPushMessage();
        message.setPage(null);

        //when
        final WnsToast toast = sender.createToastMessage(message);

        //then
        assertThat(toast.launch).isNull();
    }

    private Message getUnifiedPushMessage() {
        Message message = new Message();
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("key", "value");
        data.put("ke2", "value2");
        message.setPage("/Root.xaml");
        message.setUserData(data);
        return message;
    }
}
