package org.jboss.aerogear.unifiedpush.message.sender;

import org.apache.http.HttpStatus;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.jboss.aerogear.unifiedpush.api.FlatPushMessageInformation;
import org.jboss.aerogear.unifiedpush.api.PushApplication;
import org.jboss.aerogear.unifiedpush.api.WebPushVariant;
import org.jboss.aerogear.unifiedpush.dao.FlatPushMessageInformationDao;
import org.jboss.aerogear.unifiedpush.message.Message;
import org.jboss.aerogear.unifiedpush.message.NotificationDispatcher;
import org.jboss.aerogear.unifiedpush.message.UnifiedPushMessage;
import org.jboss.aerogear.unifiedpush.message.webpush.WebPushSender;
import org.jboss.aerogear.unifiedpush.service.ClientInstallationService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;

import java.security.Security;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.apache.http.HttpStatus.SC_GONE;
import static org.apache.http.HttpStatus.SC_NOT_FOUND;
import static org.apache.http.HttpStatus.SC_REQUEST_TOO_LONG;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;

/**
 * This class will test {@link org.jboss.aerogear.unifiedpush.message.webpush.WebPushSender } golden scenarios
 * as well as error handling.
 */

@RunWith(MockitoJUnitRunner.class)
public class WebPushSenderTest {

    private static final String TOKEN = Base64.getEncoder().encodeToString("{\"endpoint\":\"http://localhost:5309/send\",\"keys\":{\"p256dh\":\"BNrYBAc87+z7mFp8Jx8RoEZu/bYQVNJGd6ddAyuQnY9MnNpalbAbAYIHQ1T2kTU/mZCpbIs0NH4yYxAMsAuLCAI=\",\"auth\":\"vpD1pBCVtFi0usZumLYjYw==\"}}".getBytes());
    private static final UnifiedPushMessage MESSAGE;

    private ClientAndServer mockServer;

    @Mock
    FlatPushMessageInformationDao flatPushMessageInformationDao;

    @Mock
    ClientInstallationService clientInstallationService;

    @Mock
    NotificationDispatcher dispatcher;

    static {
        //Create UPS MESSAGE
        MESSAGE = new UnifiedPushMessage();

        Message message = new Message();

        message.setAlert("HELLO!");
        message.getApns().setActionCategory("some value");
        message.setSound("default");
        message.setBadge(2);
        message.getApns().setContentAvailable(true);
        MESSAGE.setMessage(message);

        //Add BC
        Security.addProvider(new BouncyCastleProvider());
    }

    private WebPushSender sender;

    private PushApplication pushApplication;
    private WebPushVariant pushVariant;



    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        //Create push Application
        pushApplication = new PushApplication();
        pushApplication.setDescription("desc");
        pushApplication.setDeveloper("Admin");
        pushApplication.setName("MyPushApp");

        pushVariant = new WebPushVariant();
        pushVariant.setPrivateKey("FTg6q0-BXP6m-i6cNpg8P6JKccCUwWaD4yuirotxqXo");
        pushVariant.setPublicKey("BIk8YK3iWC3BfMt3GLEghzY4v5GwaZsTWKxDKm-FZry3Nx2E_q-4VW3501DkQ5TX1Pe7c3yIsajUk9hQAo3sT-0");
        pushVariant.setAlias("mailto:test@test.com");
        pushVariant.setId("Id");
        pushApplication.getVariants().add(pushVariant);

        //setup flatPushDao
        when(flatPushMessageInformationDao.find(Matchers.anyString())).thenReturn(new FlatPushMessageInformation());
        sender = new WebPushSender(flatPushMessageInformationDao, dispatcher, clientInstallationService);
    }

    @After
    public void tearDown() {
        mockServer.stop();
    }

    @Test
    public void testMessageSent() throws InterruptedException {
        respondWith(HttpStatus.SC_CREATED);
        final AtomicBoolean succesRef = new AtomicBoolean(false);
        final AtomicReference<String> errorMessageRef = new AtomicReference<>("");
        final CountDownLatch latch = new CountDownLatch(1);
        NotificationSenderCallback callback = callback(succesRef, errorMessageRef, latch);

        List<String> tokenList = new ArrayList<>();
        tokenList.add(TOKEN);
        sender.sendPushMessage(pushVariant, tokenList, MESSAGE, "42", callback);

        latch.await(1, TimeUnit.SECONDS);
        assertEquals("", errorMessageRef.get());
        assertTrue(succesRef.get());
    }

    @Test
    public void testReschedule() throws InterruptedException {
        respondWith(429);
        final AtomicBoolean succesRef = new AtomicBoolean(false);
        final AtomicReference<String> errorMessageRef = new AtomicReference<>("");
        final CountDownLatch latch = new CountDownLatch(1);
        NotificationSenderCallback callback = callback(succesRef, errorMessageRef, latch);

        List<String> tokenList = new ArrayList<>();
        tokenList.add(TOKEN);
        sender.sendPushMessage(pushVariant, tokenList, MESSAGE, "42", callback);

        latch.await(1, TimeUnit.SECONDS);
        assertEquals("", errorMessageRef.get());
        assertTrue(succesRef.get());
        verify(dispatcher, times(1)).sendMessagesToPushNetwork(anyObject());
    }


    @Test
    public void testGone() throws InterruptedException {
        respondWith(SC_GONE);
        final AtomicBoolean succesRef = new AtomicBoolean(false);
        final AtomicReference<String> errorMessageRef = new AtomicReference<>("");
        final CountDownLatch latch = new CountDownLatch(1);
        NotificationSenderCallback callback = callback(succesRef, errorMessageRef, latch);

        List<String> tokenList = new ArrayList<>();
        tokenList.add(TOKEN);
        sender.sendPushMessage(pushVariant, tokenList, MESSAGE, "42", callback);

        latch.await(1, TimeUnit.SECONDS);
        assertEquals("", errorMessageRef.get());
        assertTrue(succesRef.get());
        verify(clientInstallationService, times(1)).removeInstallationsForVariantByDeviceTokens(anyString(), Matchers.argThat(includes(TOKEN)));
    }


    @Test
    public void testNotFound() throws InterruptedException {
        respondWith(SC_NOT_FOUND);
        final AtomicBoolean succesRef = new AtomicBoolean(false);
        final AtomicReference<String> errorMessageRef = new AtomicReference<>("");
        final CountDownLatch latch = new CountDownLatch(1);
        NotificationSenderCallback callback = callback(succesRef, errorMessageRef, latch);

        List<String> tokenList = new ArrayList<>();
        tokenList.add(TOKEN);
        sender.sendPushMessage(pushVariant, tokenList, MESSAGE, "42", callback);

        latch.await(1, TimeUnit.SECONDS);
        assertEquals("", errorMessageRef.get());
        assertTrue(succesRef.get());
        verify(clientInstallationService, times(1)).removeInstallationsForVariantByDeviceTokens(anyString(), Matchers.argThat(includes(TOKEN)));
    }

    @Test
    public void testTooLong() throws InterruptedException {
        respondWith(SC_REQUEST_TOO_LONG);
        final AtomicBoolean succesRef = new AtomicBoolean(false);
        final AtomicReference<String> errorMessageRef = new AtomicReference<>("");
        final CountDownLatch latch = new CountDownLatch(1);
        NotificationSenderCallback callback = callback(succesRef, errorMessageRef, latch);

        List<String> tokenList = new ArrayList<>();
        tokenList.add(TOKEN);
        sender.sendPushMessage(pushVariant, tokenList, MESSAGE, "42", callback);

        latch.await(1, TimeUnit.SECONDS);
        assertEquals("Request was too long. Message id 42", errorMessageRef.get());
    }

    @Test
    public void testBadRequest() throws InterruptedException {
        respondWith(SC_BAD_REQUEST);
        final AtomicBoolean succesRef = new AtomicBoolean(false);
        final AtomicReference<String> errorMessageRef = new AtomicReference<>("");
        final CountDownLatch latch = new CountDownLatch(1);
        NotificationSenderCallback callback = callback(succesRef, errorMessageRef, latch);

        List<String> tokenList = new ArrayList<>();
        tokenList.add(TOKEN);
        sender.sendPushMessage(pushVariant, tokenList, MESSAGE, "42", callback);

        latch.await(1, TimeUnit.SECONDS);
        assertEquals("Bad request. Message id 42", errorMessageRef.get());
    }


    private BaseMatcher<Set<String>> includes(String token) {
        return new BaseMatcher<Set<String>>() {
            @Override
            public boolean matches(Object item) {
                return ((Set<String>)item).contains(TOKEN);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("Expected to include" + TOKEN);
            }
        };
    }

    private NotificationSenderCallback callback(AtomicBoolean succesRef, AtomicReference<String> errorMessageRef, CountDownLatch latch) {
        return new NotificationSenderCallback() {
            @Override
            public void onSuccess() {
                succesRef.set(true);
                latch.countDown();
            }

            @Override
            public void onError(String reason) {
                errorMessageRef.set(reason);
                latch.countDown();
            }
        };
    }

    private void respondWith(Integer responseCode) {
        //Setup mock server
        mockServer = startClientAndServer(5309);
        mockServer.when(
                HttpRequest.request().withPath("/send")
        ).respond(
                HttpResponse.response().withStatusCode( responseCode)
        );
    }
}
