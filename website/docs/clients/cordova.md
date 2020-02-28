---
id: cordova-client
sidebar_label: Cordova Client
title: Configuring a Cordova Client
---


:::important
Before you begin configuring a Cordova application to receive push messages, you should have the following installed, available, or configured as appropriate:
* Java 8 (no newer versions since they would cause errors with the `advmanager`)
* Node.js
* Cordova (see [Cordova Documentation](https://cordova.apache.org/docs))
* [Android Studio](https://developer.android.com/studio) if developing for an Android platform
* XCode if developing for an iOS platform
:::

In this document we will give step by step instructions to build a very simple application that is able to receive push notifications
using the _aerogear push sdk_.
 
:::tip Running the samples in the android emulator
This documentation will use the `cordova emulate android` command to run the samples. This command expects the emulator to be already up and running.
To start the emulator use the `emulator @AVDMNAME -no-snapshot-load` commmand, replacing *AVDNAME* with your AVD.
To have the list of AVDS use `emulator -list-avds`
The `emulator` command can be found in the `emulator` folder of the _Android SDK_.
::: 

## Create a new project

To create a new cordova application, run the following command:

```bash
cordova create upshello org.aerogear.PushHelloWorld UpsHelloWorld
cd upshello
cordova platform add ios
cordova platform add android
```

This will create an application named `UpsHelloWorld` inside the `upshello` folder with package `org.aerogear.PushHelloWorld`.
The two `platform add` commands will add the platforms we want to target (in this example _iOS_ and _Android_).

Now check that all the requirements are installed with:

```bash
cordova requirements
```

If any of the requirements is not satisfied, fix them before continuing.

Two commonly missing requirements are:
* The SDK. To install the SDK:
    1. Open _Android Studio_
    2. Click on _Tools -> SDK Manager_
    3. Select _Android SDK_ and check _Android 9.0 Pie_
    4. Click on _Apply_
* The `gradle` tool. The installation of `gradle` depends on the platform you are running on (see [Gradle Installation](https://gradle.org/install/))

If all the requisites are satisfied, test that everything works by running:

<!--DOCUSAURUS_CODE_TABS-->
<!--Android-->
```bash
cordova build
cordova emulate android
```
<!--iOS-->
```bash
cordova build
cordova emulate ios
```
<!--END_DOCUSAURUS_CODE_TABS-->

:::important
To avoid _signing issues_ when running on iOS, do not connect any iOS device to your development machine.
:::

### Installing the push libraries

To be able to use the _push sdk_ you will need to install the following packages:
* cordova-plugin-device: this is used by the SDK to detect the platform the sdk is running on
* @aerogear/cordova-plugin-aerogear-push: this is the cordova plugin for the _push sdk_
* @aerogear/push: this is the _push sdk_ package

To install those packages, run:

```bash
cordova plugin add @aerogear/cordova-plugin-aerogear-push
cordova plugin add cordova-plugin-device
npm install --save @aerogear/push
```

:::note
If you are targeting an android device, you will need to put the `google-services.json` file into the `platforms/android/app` folder.
To get that file, you must create an app in your _FireBase_ account (with package `org.aerogear.PushHelloWorld`) and download it from there. 
:::

### Installing webpack
The following steps will be needed to correctly install webpack:
1. install the `cordova-plugin-webpack` plugin
    ```bash
    $ cordova plugin add cordova-plugin-webpack    
    ```
2. create an `src` folder
    ```bash
    $ mkdir src
    ```
3. move the `index.js` file from `www/js` to `src`
    ```bash
    $ mv www/js/index.js src
    ```
4. create a configuration file for _webpack_
    ```bash
    $ echo "const path = require('path');
      module.exports = {
        mode: 'development',
        entry: './src/index.js',
        output: {
          path: path.resolve(__dirname, 'www/js'),
          filename: 'index.js',
        },
        devtool: 'inline-source-map',
      };" > webpack.config.js
    ```
### Enabling clear-text connection
If your _UnifiedPush Server_ is exposed on an `http` address, you will need to enable _clear-text_ connection in your 
application by editing the _Content Security Policy_ in your `www/index.html` file

Change
```html
<meta 
    http-equiv="Content-Security-Policy" 
    content="default-src 'self' data: gap: https://ssl.gstatic.com 'unsafe-eval'; style-src 'self' 'unsafe-inline'; media-src *; img-src 'self' data: content:;">
```
to
```html
<meta 
    http-equiv="Content-Security-Policy" 
    content="default-src 'self' data: gap: https://ssl.gstatic.com 'unsafe-eval'; style-src 'self' 'unsafe-inline'; media-src *; img-src 'self' data: content:; connect-src http://*:*">
```

:::caution
In this example, we enabled _clear-text_ on *every site*. A better choice would be to put the _IP Address_ of your _UnifiedPush Server_ instead of the _*_
:::

If you are targeting _android_ you will have to add this to your `config.xml` file
```xml
<edit-config file="app/src/main/AndroidManifest.xml" mode="merge" target="/manifest/application" xmlns:android="http://schemas.android.com/apk/res/android">
  <application android:usesCleartextTraffic="true" />
</edit-config>
```

## Register the application with the UnifiedPush Server

To register the application, you must use the `PushRegistration` object from the `@aerogear/push` package with a code
similar to the following:

```javascript
function register() {
  console.log('Registering...');
  new PushRegistration({
      url: 'http://192.168.1.187:9999',  // change this to your UPS URL
      android: { // your variant platform (android, ios or webpush)
          senderID: '829475845435',  // Your senderID as you see it in your Firebase Console
          variantID: '172bf953-f266-4e32-866b-662ff32d653c', // The id of the variant you created
          variantSecret: '7680585e-c22e-4105-b0fc-fbcb150036d4' // the secret of the variant you created
      }
  })
  .register()
  .then(() => {
      console.log('Registered!');
  })
  .catch(error => {
      console.log('Failed: ', error.message, JSON.stringify(error))
  });
}
```

## Receiving Push Notifications
Receiving _Push Notifications_ is as simple as registering a callback handler:
```javascript
PushRegistration.onMessageReceived((notification => {
  console.log('Received push notification: ', notification.message);
}));
```

## Complete code
Change the `src/index.js` file with the following content:
```javascript
import { PushRegistration } from '@aerogear/push';

var app = {
  // Application Constructor
  initialize: function() {
      document.addEventListener('deviceready', this.onDeviceReady.bind(this), false);
  },

  // deviceready Event Handler
  //
  // Bind any cordova events here. Common events are:
  // 'pause', 'resume', etc.
  onDeviceReady: function() {
    this.receivedEvent('deviceready');
  },

  // Update DOM on a Received Event
  receivedEvent: function(id) {
    var parentElement = document.getElementById(id);
    var listeningElement = parentElement.querySelector('.listening');
    var receivedElement = parentElement.querySelector('.received');

    listeningElement.setAttribute('style', 'display:none;');
    receivedElement.setAttribute('style', 'display:block;');

    this.registerToUPS();

    PushRegistration.onMessageReceived((notification => {
      console.log('Received push notification: ', notification.message);
    }));
  },

  registerToUPS: () => {
    console.log('Registering...');
    new PushRegistration({
      url: 'http://192.168.1.187:9999',  // change this to your UPS URL
      android: { // your variant platform (android, ios or webpush)
        senderID: '829475845435',  // Your senderID as you see it in your Firebase Console
        variantID: '172bf953-f266-4e32-866b-662ff32d653c', // The id of the variant you created
        variantSecret: '7680585e-c22e-4105-b0fc-fbcb150036d4' // the secret of the variant you created
      }
    })
    .register()
    .then(() => {
      console.log('Registered!');
    })
    .catch(error => {
      console.log('Failed: ', error.message, JSON.stringify(error))
    });
  }
};

app.initialize();
``` 
## Running the application
The application can run in the emulator for Android, while it needs to run on a real device for iOS (the iOS simulator 
does not support _push notifications_). 

<!--DOCUSAURUS_CODE_TABS-->
<!--Android-->
1. Be sure that the emulator is already up and running
2. Issue `cordova emulate android`
<!--iOS-->
1. Connect an iOS device to the USB port of your mac
1. Run _XCode_ with
    ```bash
    $ open platforms/ios/UpsHelloWorld.xcworkspace
    ```
2. Configure the _Signing and Capabilities_
3. Click on the _run_ icon
<!--END_DOCUSAURUS_CODE_TABS-->

### Throubleshooting
#### `cordova emulator android` takes forever to install the app into the emulator

This can happen when the `cordova emulator android` command is not able to install the app into the emulator.
To solve the issue, simply close the emulator and run it again with 
```bash
emulator @AVDNAME -no-snapshot-load
```
