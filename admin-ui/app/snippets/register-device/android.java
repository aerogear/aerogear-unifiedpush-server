package com.push.pushapplication;

import java.net.URI;
import java.net.URISyntaxException;

import org.jboss.aerogear.android.unifiedpush.PushConfig;
import org.jboss.aerogear.android.unifiedpush.PushRegistrar;
import org.jboss.aerogear.android.unifiedpush.Registrations;

import android.app.Application;

public class PushApplication extends Application {

    private final String VARIANT_ID       = "{{ variant.variantID }}";
    private final String SECRET           = "{{ variant.secret }}";
    private final String GCM_SENDER_ID    = "{{ variant.projectNumber }}";
    private final String UNIFIED_PUSH_URL = "{{ contextPath }}";

    private PushRegistrar registration;

    @Override
    public void onCreate() {
        super.onCreate();

        Registrations registrations = new Registrations();

        try {
            PushConfig config = new PushConfig(new URI(UNIFIED_PUSH_URL), GCM_SENDER_ID);
            config.setVariantID(VARIANT_ID);
            config.setSecret(SECRET);
            config.setAlias(MY_ALIAS);

            registration = registrations.push("unifiedpush", config);

            registration.register(getApplicationContext(), new Callback() {
                private static final long serialVersionUID = 1L;

                @Override
                public void onSuccess(Void ignore) {
                     Toast.makeText(MainActivity.this, "Registration Succeeded!",
                             Toast.LENGTH_LONG).show();
               }

               @Override
               public void onFailure(Exception exception) {
                     Log.e("MainActivity", exception.getMessage(), exception);
               }
            });

        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
