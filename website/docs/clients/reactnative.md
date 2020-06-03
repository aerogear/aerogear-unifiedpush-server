---
id: reactnative-client
sidebar_label: React Native Client
title: Configuring a React Native Client
---
[React Native](reactnative.dev) is a framework that allows you to write mobile applications using React. Much like Cordova, you can access and use UnifiedPush in your React Natrive source with a single API. Because push networks are platform dependent, there is some native project configuration required. It is also recommended that you familiarize yourself with the [Android client](./android-client) and [iOS client](./ios-client) documentation.

## Overview
You will be guided through how to add the UPS library, configure your native applications, connect to UPS, and receive push messages. 

## React Native Setup
As our first step we need to install the react library.

```yarn install @aerogear/aerogear-reactnative-push```

Once we've done this we can begin configuring our native projects. 

## Android Native Project configuration

Open the `android` project found in your React Native project root in Android Studio, and begin integrating Firebase Cloud messaging into the application. This is most easily done using the Firebase Assistant, and you can find that documentation in the [Android client documentation](./android-client)


## iOS Native Project configuration

Run `pod install` from the `ios` project found in your React Native project root from the command line before you open this project in XCode. Now you should follow the [iOS client documentation](./ios-client) to enable APNS push messaging for your project.

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

```
ups.register({
      "alias":"rnAlias",
      "categories":["cat1", "cat2"]
}).then(()=>{
  //You are registered, inform your app
})

## Register Message Handlers

A message handler is a function that takes a single parameter, the message. This message is your push notification data. You can register the message handler with the following code : 

```
const ups = new RNUnifiedPush();
let callback = (message)=>{
  console.log("You have receieved a background push message." + JSON.stringify(message));
};

ups.registerMessageHandler(callback);
```

## Hello World Example

The AeroGear project provides an example UnifiedPush application in our [unifiedpush-cookbook repository](https://github.com/aerogear/unifiedpush-cookbook/tree/master/react-native/push)
