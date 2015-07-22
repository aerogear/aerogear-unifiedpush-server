package com.push.pushapplication;

import android.util.Log;
import android.app.Application;

import org.jboss.aerogear.android.core.Callback;
import org.jboss.aerogear.android.unifiedpush.PushRegistrar;
import org.jboss.aerogear.android.unifiedpush.RegistrarManager;
import org.jboss.aerogear.android.unifiedpush.gcm.AeroGearGCMPushConfiguration;

public class PushApplication extends Application {

    private final String VARIANT_ID       = "{{ variant.variantID }}";
    private final String SECRET           = "{{ variant.secret }}";
    private final String GCM_SENDER_ID    = "{{ variant.projectNumber }}";
    private final String UNIFIED_PUSH_URL = "{{ contextPath }}";

    @Override
    public void onCreate() {
        super.onCreate();

        RegistrarManager.config("register", AeroGearGCMPushConfiguration.class)
                    .setPushServerURI(URI.create(UNIFIED_PUSH_URL))
                    .setSenderIds(GCM_SENDER_ID)
                    .setVariantID(VARIANT_ID)
                    .setSecret(SECRET)
                    .asRegistrar();

        PushRegistrar registrar = RegistrarManager.getRegistrar("register");
        registrar.register(getApplicationContext(), new Callback<Void>() {
            @Override
            public void onSuccess(Void data) {
                Log.i(TAG, "Registration Succeeded!");
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, exception.getMessage(), exception);
            }
        });

    }
}
