---
id: reactnative-client
sidebar_label: React Native Client
title: Configuring a React Native Client
---
[React Native](https://reactnative.dev) is a framework that allows you to write mobile applications using React. Much like Cordova, you can access and use UnifiedPush in your React Native source with a single API. Because push networks are platform dependent, there is some native project configuration required. It is also recommended that you familiarize yourself with the [Android client](./android-client) and [iOS client](./ios-client) documentation.

## Overview
You will be guided through how to add the UPS library, configure your native applications, connect to UPS, and receive push messages. 

## React Native Setup
As our first step we need to install the react library.

```npm install @aerogear/aerogear-reactnative-push```

Once we've done this we can begin configuring our native projects. 

## Android Native Project configuration

Open the `android` project found in your React Native project root in Android Studio, and begin integrating Firebase Cloud Messaging into the application. This is most easily done using the Firebase Assistant, and you can find that documentation in the [Android client documentation](./android-client)


## iOS Native Project configuration

Run `pod install` from the `ios` project found in your React Native project root from the command line before you open this project in XCode. 
Now you should follow the [iOS client documentation](./ios-client#add-required-capabilities) to enable APNS push messaging for your project.

### Enable React Native Unified Push Integration 

There are two ways to enable the integration:
* Extending the `UPSEnabledAppDelegate`
* Manually adding the integration

#### Extending the `UPSEnabledAppDelegate` class

1. Open the iOS project by running `xed ios` from the root of the react-native project
2. In XCode open your `AppDelegate.h` header file and change its code from
    ```objective-c
    #import <React/RCTBridgeDelegate.h>
    #import <UIKit/UIKit.h>

    @interface AppDelegate : UIResponder <UIApplicationDelegate, RCTBridgeDelegate>

    @property (nonatomic, strong) UIWindow *window;

    @end
    ```
    to
    ```objective-c
    #import <React/RCTBridgeDelegate.h>
    #import <UIKit/UIKit.h>
    #import <UPSEnabledAppDelegate.h>

    @interface AppDelegate : UPSEnabledAppDelegate

    @property (nonatomic, strong) UIWindow *window;

    @end
    ```
3. Open your `AppDelegate.m` file and add a call to `[super application:application didFinishLaunchingWithOptions:launchOptions];` into your `didFinishLaunchingWithOptions` method. It should look like:
    ```objective-c
    - (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions
    {
    #if DEBUG
      InitializeFlipper(application);
    #endif
    
      RCTBridge *bridge = [[RCTBridge alloc] initWithDelegate:self launchOptions:launchOptions];
      RCTRootView *rootView = [[RCTRootView alloc] initWithBridge:bridge
                                                       moduleName:@"push"
                                                initialProperties:nil];
    
      rootView.backgroundColor = [[UIColor alloc] initWithRed:1.0f green:1.0f blue:1.0f alpha:1];
    
      self.window = [[UIWindow alloc] initWithFrame:[UIScreen mainScreen].bounds];
      UIViewController *rootViewController = [UIViewController new];
      rootViewController.view = rootView;
      self.window.rootViewController = rootViewController;
      [self.window makeKeyAndVisible];
      
      
      // Enable Push Notifications
      [super application:application didFinishLaunchingWithOptions:launchOptions];
      
      return YES;
    }
    ```
   
#### Manually integrate the Unified Push
:::warning
This steps are needed *only* if you are not extending the `UPSEnabledAppDelegate` class
:::

1. Open the iOS project by running `xed ios` from the root of the react-native project
2. Open you `AppDelegate.m` file
3. Add the following code before the end of your `didFinishLaunchingWithOptions` method:
    ```objective-c
    - (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions
    {
      ...
      ...
      // This is the code to be added:
      // Enable Push Notifications
      UNUserNotificationCenter *center = [UNUserNotificationCenter currentNotificationCenter];
      [center requestAuthorizationWithOptions:(UNAuthorizationOptionAlert + UNAuthorizationOptionBadge + UNAuthorizationOptionSound) completionHandler:^(BOOL granted, NSError * _Nullable error) {
      
      }];
      [[UIApplication sharedApplication] registerForRemoteNotifications];
      return YES;
    }
    ```
4. Pass the `deviceToken` to the UnifiedPush library as soon as registration is done by adding the method:
    ```objective-c
    - (void)application:(UIApplication *)application didRegisterForRemoteNotificationsWithDeviceToken:(NSData *)deviceToken {
      [RnUnifiedPush didRegisterForRemoteNotificationsWithDeviceToken:deviceToken];
    }
    ```
5. Forward the notifications to the UnifiedPush library by adding the following method:
    ```objective-c
    - (void)application:(UIApplication *)application didReceiveRemoteNotification:(NSDictionary *)userInfo fetchCompletionHandler:(void (^)(UIBackgroundFetchResult))completionHandler {
      [RnUnifiedPush didReceiveRemoteNotification:userInfo];
    }
    ```


## Configuring Unified Push

Unified Push requires an Android or iOS variant to be configured before it can send push messages to your React Native application. Please refer to the [Android](../variants/android) and [iOS](../variants/ios) variant configuration sections for step by step guides.

## Integrating Unified Push into Your Application.

To receive messages sent by Unified Push you need to initialize the library, register your device, and then register a message handler. This process will be detailed in the next few sections.

### Initialize the Library

First we'll import the push library and create our Unified Push Server object.

```javascript
import RNUnifiedPush from '@aerogear/aerogear-reactnative-push';

const ups = new RNUnifiedPush();
```

We need to call `ups.init` to initialize our library. This method takes one parameter, the `push-config` object and returns a promise. In the following example we are providing the variant registration information for both iOS and Android. 
```javascript
 ups.init(
            { 
              pushServerURL: "http://10.1.10.80:9999/",
              ios: {
                variantID: "91c039f9-d657-49cd-b507-cb78bea786e3",
                variantSecret: "4b7fd0b4-58b5-46e8-80ef-08a6b8d449cd"
              }, 
              android: {
                senderID: "1234567890",
                variantID: "77fc90fa-6c79-4ed7-a699-36861b0d309e",
                variantSecret: "0625eca0-3b76-4614-bdc6-2d40da6195e4"
              }
            }).catch(err => console.log("Error Initializing", err));
```

With an initialized library, we may now register with the Unified Push server. Registration should be called every time you load your application and any time you wish to change your user's alias or category. As a reminder, an alias is an identifier for your user shared among their devices (such as an email or username), and categories are subscriptions to messages that are sent to groups of users. The following example registers a user with an alias subscribed to two categories.

```javascript
ups.register({
      "alias":"rnAlias",
      "categories":["cat1", "cat2"]
}).then(()=>{
  //You are registered, inform your app
})
```

## Register Message Handlers

RNUnifiedPush instances receive messages from the native platform the application is running on. The library passes these messages largely as is and the developer is responsible for ensuring that their application handles both iOS and Android messages. Do this by checking for the `aps` property of the message object, or by using [platform specific code](https://reactnative.dev/docs/platform-specific-code). The following example uses different code files for Android and iOS to define our handler, and then registers to UnifiedPush using a RNUnifiedPush instance.

*handler.android.js*
```javascript
//Extract the message text and add it to a state variable in a React application
export default function(message) { this.setState({messages: [...this.state.messages, message.alert] })}
```

*handler.ios.js*
```javascript
//Extract the message text and add it to a state variable in a React application
export default function(message) {this.setState({messages: [...this.state.messages, message.aps.alert.body] })};

```

*app.js*
```javascript
    import handler from './handler';
    /*
        SNIPPING imports and registration
    */
    ups.registerMessageHandler(handler.bind(this));  //.bind(this) allows `this` to work in the above examples
```

## Hello World Example

The AeroGear project provides an example UnifiedPush application in our [unifiedpush-cookbook repository](https://github.com/aerogear/unifiedpush-cookbook/tree/master/react-native/push).
