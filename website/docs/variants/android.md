---
id: android
title: Android Variant
---


## Obtaining Firebase Cloud Messaging Credentials

Before the Android application is able to receive notifications, you must set up access to Firebase Cloud Messaging. You also need an emulator with Google Services or an Android device.  This section shows you how to obtain the **Server key** and the **Sender ID** from Firebase Cloud Messaging for configuring Unified Push.

 Start by opening the [Firebase Console](https://console.firebase.google.com).

1. If you haven't created a project in the firebase console, please do so.

2. In your project side bar, click on the gear icon in the navigation column by "Project Overview" to open a menu.  Select "Project settings" from that menu.

> ![Project Number](assets/android/project_settings.png)

3. On the _Project Settings_ screen, switch to the _Cloud Messaging_ tab, where you can find the **Server key** and **Sender ID**.  Make a note of these values, we will use them to configure the variant in the next section.

> ![Retrieve Credentials](assets/android/retrieve_credentials.png)

## Configure the Variant

With all the Google work being done, we are now ready to setup the [UnifiedPush Server](https://github.com/aerogear/aerogear-unified-push-server), so that it can be used to connect to FCM for later message sending.

In the wizard after you create a PushApplication, click the **Add Variant** button and fill out the Android options. You will want to use the **Server Key** and **Sender ID** obtained from the *Firebase Console* in their appropriate fields:

> ![Android Variant Options](assets/android/variant_01.png)

Afterwards you will see some code snippets, containing the **Variant ID** and **Secret**, that you can use in your Android application for the registration of the device, running your app:

> ![Android Variant Details](assets/android/variant_02.png)

Unified Push is now configured, configuring the Android client is covered in [the client guide](../clients/android.md).