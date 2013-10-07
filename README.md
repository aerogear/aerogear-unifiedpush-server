# AeroGear UnifiedPush Server [![Build Status](https://travis-ci.org/aerogear/aerogear-unifiedpush-server.png)](https://travis-ci.org/aerogear/aerogear-unifiedpush-server)

The _AeroGear UnifiedPush Server_ is a server that allows sending push notifications to different (mobile) platforms. The initial version of the server supports [Apple’s APNs](http://developer.apple.com/library/mac/#documentation/NetworkingInternet/Conceptual/RemoteNotificationsPG/Chapters/ApplePushService.html#//apple_ref/doc/uid/TP40008194-CH100-SW9), [Google Cloud Messaging](http://developer.android.com/google/gcm/index.html) and [Mozilla’s SimplePush](https://wiki.mozilla.org/WebAPI/SimplePush).

<img src="http://people.apache.org/~matzew/AdminUI.png" height="303px" width="510px" />

### Client library support

Besides the server-side offerings, the AeroGear project supports a few platforms regarding push.

#### Android

The [AeroGear Android](https://github.com/aerogear/aerogear-android) library has support for device registration with the UnifiedPush Server. In addition it also comes with a handy message listener interface to ease the work of receiving notifications within an Android app.

#### iOS

For iOS there is a little [helper library](https://github.com/aerogear/aerogear-push-ios-registration) that performs registration of the device against the UnifiedPush Server. 

#### JavaScript

The [AeroGear.js](https://github.com/aerogear/aerogear-js) library has support for device registration with the UnifiedPush Server. This can be used from Apache Cordova applications as well. Besides that, AeroGear.js comes with a polyfill implementation of Mozilla's SimplePush API, which makes it easy to run SimplePush in any browser, there is **_no_** limitation to Firefox OS or the Firefox browsers.

### Getting started with the server

Starting the JBoss Application Server:

```
./bin/standalone.sh -b 0.0.0.0
```

Deploying the server to JBoss AS using the jboss-as-maven-plugin:

```
mvn package jboss-as:deploy
```

***Note:** When testing functionality with the included webapp, it may be necessary to clear the browser's local storage occasionally to get accurate testing results. This is due to the client library storing channel information for later reuse after losing a connection (via refresh, browser close, internet drop, etc.) The functionality to cleanly handle this issue is in development and will be added soon thus removing the need for manual local storage cleanup. Consult your browser's docs for help with removing items from local storage.


#### Administration Console

Once the server is running access it via ```http://SERVER:PORT/ag-push```. Check the Administration console [user guide](http://aerogear.org/docs/guides/AdminConsoleGuide/) for more information on using the console.

Besides the _Administration console_ the server can be accessed over RESTful APIs, as explained below.


#### HTTPS

We highly recommend using the _AeroGear UnifiedPush Server_ via *https*! _Note: The ```cURL``` statements below are using SSLv3 (```-3```)_

#### Login

Temporarily there is a "admin:123" user.  On _first_ login,  you will need to change the password.

```
curl -3 -v -b cookies.txt -c cookies.txt
  -H "Accept: application/json" -H "Content-type: application/json"
  -X POST -d '{"loginName": "admin", "password":"123"}'
  https://SERVER:PORT/CONTEXT/rest/auth/login
```

This will return a status code of 403, then do perform the update:

```
curl -3 -v -b cookies.txt -c cookies.txt
  -H "Accept: application/json" -H "Content-type: application/json"
  -X PUT -d '{"loginName": "admin", "password":"123", "newPassword":"SOMENEWPASSWORD"}'
  https://SERVER:PORT/CONTEXT/rest/auth/update
```

To _update_ the initial password, you need to specify the old ```password``` and new password (```newPassword```). Now proceed with a login, by using the new password:

```
curl -3 -v -b cookies.txt -c cookies.txt
  -H "Accept: application/json" -H "Content-type: application/json"
  -X POST -d '{"loginName": "admin", "password":"SOMENEWPASSWORD"}'
  https://SERVER:PORT/CONTEXT/rest/auth/login
```


#### Register Push App

Register a ```PushApplication```, like _Mobile HR_:

```
curl -3 -v -b cookies.txt -c cookies.txt -v -H "Accept: application/json" -H "Content-type: application/json"
  -X POST
  -d '{"name" : "MyApp", "description" :  "awesome app" }'
  https://SERVER:PORT/CONTEXT/rest/applications
```

_The response returns a **pushApplicationID** and a **masterSecret** that will be both used later on when you attempt to send a push message (either Broadcast or Selected Send)._

##### iOS Variant

Add a *PRODUCTION* ```iOS``` variant (e.g. _HR for iOS_):
```
curl -3 -v -b cookies.txt -c cookies.txt
  -i -H "Accept: application/json" -H "Content-type: multipart/form-data"
  -F "certificate=@/Users/matzew/Desktop/MyProdCert.p12"
  -F "passphrase=TopSecret"
  -F "production=true"  // make sure you have Production certificate and Provisioning Profile

  -X POST https://SERVER:PORT/CONTEXT/rest/applications/{pushApplicationID}/iOS
```

Add a *DEVELOPMENT* ```iOS``` variant (e.g. _HR for iOS_):
```
curl -3 -v -b cookies.txt -c cookies.txt
  -i -H "Accept: application/json" -H "Content-type: multipart/form-data"
  -F "certificate=@/Users/matzew/Desktop/MyTestCert.p12"
  -F "passphrase=TopSecret"
  -F "production=false"  // make sure you have Development certificate and Provisioning Profile

  -X POST https://SERVER:PORT/CONTEXT/rest/applications/{pushApplicationID}/iOS
```

**NOTE:** The above is a _multipart/form-data_, since it is required to upload the "Apple Push certificate"!

_The response returns a **variantID** and a **secret**, that will be both used later on when registering your installation through the iOS client SDK._

##### Android Variant

Add an ```android``` variant (e.g. _HR for Android_):
```
curl -3 -v -b cookies.txt -c cookies.txt
  -v -H "Accept: application/json" -H "Content-type: application/json"
  -X POST
  -d '{"googleKey" : "IDDASDASDSA"}'

  https://SERVER:PORT/CONTEXT/rest/applications/{pushApplicationID}/android
```

_The response returns a **variantID** and a **secret**, that will be both used later on when registering your installation through the Android client SDK._

##### SimplePush Variant

Add an ```simplepush``` variant (e.g. _HR for Browser):
```
curl -3 -v -b cookies.txt -c cookies.txt
  -v -H "Accept: application/json" -H "Content-type: application/json"
  -X POST
  -d '{"name" : "My SimplePush Variant"}'

  https://SERVER:PORT/CONTEXT/rest/applications/{pushApplicationID}/simplePush
```

_The response returns a **variantID** and a **secret**, that will be both used later on when registering your installation through the UnifiedPush JS SDK._

#### Registration of an installation, for an iOS device:

iOS example for performing registration of a client:

```objective-c
- (void)application:(UIApplication*)application
  didRegisterForRemoteNotificationsWithDeviceToken:(NSData*)deviceToken
    AGDeviceRegistration *registration =

        [[AGDeviceRegistration alloc] initWithServerURL:[NSURL URLWithString:@"<# URL of the running AeroGear UnifiedPush Server #>"]];

    [registration registerWithClientInfo:^(id<AGClientDeviceInformation> clientInfo) {
        [clientInfo setDeviceToken:deviceToken];

        [clientInfo setVariantID:@"<Variant Id #>"];
        [clientInfo setVariantSecret:@"<Variant Secret>"];

        // --optional config--
        // set some 'useful' hardware information params
        UIDevice *currentDevice = [UIDevice currentDevice];

        [clientInfo setOperatingSystem:[currentDevice systemName]];
        [clientInfo setOsVersion:[currentDevice systemVersion]];
        [clientInfo setDeviceType: [currentDevice model]];

    } success:^() {
        // successfully registered!
    } failure:^(NSError *error) {
        NSLog(@"PushEE registration Error: %@", error);
    }];
```

Check the [iOS client SDK page](https://github.com/aerogear/aerogear-push-ios-registration) for more information.

#### Registration of an installation, for an Android device:

Android example for performing registration of a client:

```java
// collect all 'PushRegistrar' objects:
private final Registrations registrations = new Registrations();

// Create a PushConfig for the UnifiedPush Server:
PushConfig config = new PushConfig(UNIFIED_PUSH_URL, GCM_SENDER_ID);
config.setVariantID(VARIANT_ID);
config.setSecret(SECRET);
config.setAlias(MY_ALIAS);

// create an actual 'PushRegistrar' to register with the UnifiedPush Server:
PushRegistrar registrar = registrations.push("u", config);

// register with the UnifiedPush Server:
registrar.register(getApplicationContext(), new Callback<Void>() {
    ...
    @Override
    public void onSuccess(Void ignore) {
      // device metadata stored on UnifiedPush Server:
    }

    @Override
    public void onFailure(Exception exception) {
        // something went wrong
    }
});
```

#### Registration of an installation, for a JavaScript/SimplePush client:

JavaScript example for performing registration of a client:

```javascript
//Create the UnifiedPush client object:
var client = AeroGear.UnifiedPushClient(
    "myVariantID",
    "myVariantSecret",
    "http://SERVER:PORT/CONTEXT/rest/registry/device"
);

// assemble the metadata for the registration:
var metadata = {
    deviceToken: "theDeviceToken",
    alias: "some_username",
    category: "email",
    simplePushEndpoint: "https://some.server.com/something"
};

// perform the registration against the UnifiedPush server:
client.registerWithPushServer(metadata);
```

### Sender

To send a message (version) notification, issue the following command:

```
curl -3 -u "{PushApplicationID}:{MasterSecret}"
   -v -H "Accept: application/json" -H "Content-type: application/json"
   -X POST

   -d '{
      "variants" : ["c3f0a94f-48de-4b77-a08e-68114460857e", "444939cd-ae63-4ce1-96a4-de74b77e3737" ....],
      "category" : "someCategory",
      "alias" : ["user@account.com", "jay@redhat.org", ....],
      "deviceType" : ["iPad", "AndroidTablet", "web"],

      "message": {"key":"value", "key2":"other value", "alert":"HELLO!"},
      "simple-push": { "SomeCategory":"version=123", "anotherCategory":"version=456"}
   }'

https://SERVER:PORT/CONTEXT/rest/sender
```

For more details take a look at the ["message format specification"](http://aerogear.org/docs/specs/aerogear-push-messages/) and the [RESTful Sender API](http://aerogear.org/docs/specs/aerogear-push-rest/Sender/).



## Related documentation

#### Specifications

* [AeroGear UnifiedPush Server](http://aerogear.org/docs/specs/aerogear-server-push/)
* [Client Registration](http://aerogear.org/docs/specs/aerogear-client-push/)
* [Push Message Format](http://aerogear.org/docs/specs/aerogear-push-messages/)

#### REST APIs

Documentation for the REST APIs of the AeroGear UnifiedPush Server can be found [here](http://aerogear.org/docs/specs/aerogear-push-rest/).
