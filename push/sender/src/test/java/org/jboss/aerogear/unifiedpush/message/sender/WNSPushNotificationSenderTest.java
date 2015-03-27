package org.jboss.aerogear.unifiedpush.message.sender;

import ar.com.fernandospr.wns.model.WnsTile;
import ar.com.fernandospr.wns.model.WnsToast;
import org.jboss.aerogear.unifiedpush.message.Message;
import org.jboss.aerogear.unifiedpush.message.windows.TileType;
import org.jboss.aerogear.unifiedpush.message.windows.ToastType;
import org.jboss.aerogear.unifiedpush.message.windows.Type;
import org.jboss.aerogear.unifiedpush.message.windows.Windows;
import org.junit.Test;

import java.util.Arrays;
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
        WnsToast toastMessage = sender.createSimpleToastMessage(message);

        //then
        assertThat(toastMessage.launch).isEqualTo("/Root.xaml");
    }

    @Test
    public void shouldAddAlertAutomatically() {
        //given
        Message message = getUnifiedPushMessage();
        message.setAlert("My message");

        //when
        WnsToast toastMessage = sender.createSimpleToastMessage(message);

        //then
        assertThat(toastMessage.launch).isEqualTo("/Root.xaml" + QUERY + "&message=My+message");
    }

    @Test
    public void shouldEncodeUserDataInLaunchParam() {
        //given
        Message pushMessage = getUnifiedPushMessage();

        //when
        final WnsToast toast = sender.createSimpleToastMessage(pushMessage);

        //then
        assertThat(toast.launch).isEqualTo("/Root.xaml" + QUERY);
    }

    @Test
    public void shouldEncodeUserDataInCordovaPage() {
        //given
        Message pushMessage = getUnifiedPushMessage();
        pushMessage.getWindows().setPage("cordova");

        //when
        final WnsToast toast = sender.createSimpleToastMessage(pushMessage);

        //then
        assertThat(toast.launch).isEqualTo(WNSPushNotificationSender.CORDOVA_PAGE + QUERY);
    }

    @Test
    public void shouldNoPage() {
        //given
        Message message = getUnifiedPushMessage();
        message.getWindows().setPage(null);

        //when
        final WnsToast toast = sender.createSimpleToastMessage(message);

        //then
        assertThat(toast.launch).isNull();
    }

    @Test
    public void shouldCreateRightToastTemplate() {
        //given
        Message message = new Message();
        Windows windows = message.getWindows();
        windows.setType(Type.toast);
        windows.setToastType(ToastType.ToastImageAndText04);
        windows.setTextFields(Arrays.asList("item1", "item2"));
        message.setAlert("title");
        windows.setImages(Arrays.asList("image1.jpg"));

        //when
        WnsToast toastMessage = sender.createToastMessage(message);

        //then
        assertThat(toastMessage).isNotNull();
        assertThat(toastMessage.visual.binding.texts.get(0).value).isEqualTo("title");
        assertThat(toastMessage.visual.binding.images.get(0).src).contains("image1.jpg");
    }

    @Test
    public void shouldCreateRightTileTemplate() {
        //given
        Message message = new Message();
        Windows windows = message.getWindows();
        windows.setType(Type.tile);
        windows.setTileType(TileType.TileWideBlockAndText01);
        windows.setTextFields(Arrays.asList("item1", "item2", "item3", "item4", "item5"));
        message.setAlert("title");

        //when
        WnsTile tileMessage = sender.createTileMessage(message);

        //then
        assertThat(tileMessage).isNotNull();
        assertThat(tileMessage.visual.binding.texts.get(0).value).isEqualTo("title");
    }

    private Message getUnifiedPushMessage() {
        Message message = new Message();
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("key", "value");
        data.put("ke2", "value2");
        message.getWindows().setPage("/Root.xaml");
        message.setUserData(data);
        return message;
    }
}
