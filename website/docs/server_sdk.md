---
id: server_sdk
title: Unified Push Server SDK
sidebar_label: Unified Push Server SDK
---

This page documents how to use UPS

## Senders
### JavaSender

The _UnifiedPush Server_ supports a Java API Client for sending Push Notifications

#### Getting Started

You must add the following dependencies to your `pom.xml` file:

```xml
    <dependency>
    <groupId>org.jbos.aerogear</groupId>
    <artifactId>unifiedpush-java-client</artifactIt>
    <version>1.1.0.Final</version>
    </dependency>
```

#### Usage

You can create the `JavaSender` or alternatively you can use an external `pushConfig.json` file to configure connecting to the _UnifiedPush Server_.

Creating a `JavaSender` would look like:

```Java
Pushsender defaultPushSender = DefaultPushSender
  .withRootServerURL("<yourPushServerURL e.g http://localhost:9999/>")
  .pushApplicationId("<yourPushApplicationId e.g. B868CC08-BCC8-4A0A-B21E-1AC56AF0C734>")
  .masterSecret("<yourMasterSecret e.g. 4L30AV41-3278-4983-8F99-0EEA138J7O1I>")
  .build();
```

The external `pushConfig.json` file would look like this:

```JSON
{
  "serverUrl":"<yourPushServerURL e.g http://localhost:9999/>",
  "pushApplicationId": "<yourPushApplicationId e.g. B868CC08-BCC8-4A0A-B21E-1AC56AF0C734>",
  "masterSecret": "<yourMasterSecret e.g. 4L30AV41-3278-4983-8F99-0EEA138J7O1I>"
}
```
And then, to connect using the external `pushConfig.json` file your JavaSender would look like:

```Java
PushSender defaultPushSender = DefaultPushSender
  .withConfig("pushConfig.json")
  .build();
```
You can connect to the _UnifiedPush Server_ via proxy with the `JavaSender` API also!

```Java
PushSender defaultPushSender = DefaultPushSender
  .withConfig("pushConfig.json")
  .proxy("proxy.example.com", 8080)
  .proxyUser("proxyuser")
  .proxyPassword("password")
  .proxyType(Prox.Type.HTTP)
  .build();
```
:::note
The ability to connect via proxy is a feature only available with the JavaSender Client API
:::

You can implement your own custom `TrustStore` as follows:

```Java
PushSender defaultPushSender = DefaultPushSender
  .withConfig("pushConfig.json")
  .customTrustStore("setup/aerogear.truststore", "jks", "aerogear")
  .build();
```

#### Sending Messages

A `UnifiedMessage` represents a message in the format expected from the _UnifiedPush Server_, the format is: A generic `JSON` map is used to send messages to Android and iOS devices. The application on the devices will receive the `JSON` map and are responsible for performing a lookup to read values of the given keys

The UnifiedMessage consists of the `messageBuilder`, `ApnsBuilder`, `criteriaBuilder` and the `configBuilder` Classes, implementing the builder design pattern.

The `messageBuilder` object consists of :

- alert - Message that will be displayed on the alert UI element.
- sound - Plays a given sound, on iOS no API needs to be invoked to play a sound file.
- badge - Sets the value of the badge icon, on iOS no API needs to be invoked by the app developer.
- priority - Sets the Priority of the message, values expected are `NORMAL` or `HIGH`.
- userData - Adds a custom value for the given key, used to pass user data to the _UnifiedPush Server_

The `ApnsBuilder` object consists of iOS specific arguments:

- contentAvailable - Marks the payload as 'content-available'. This feature is needed when sending notifications to Newsstand applications and submitting silent iOS notifications. (iOS 7)
- actionCategory - Argument to pass an Action Category for interaction notifications. ( iOS8)
- action - Sets the value of the 'action' key from the submitted payload.
- title - Sets the value of the 'title' key from the submitted payload.
- localizedTitleKey - The key to a title string in the Localizable.strings file for the current localization.
- localizedTitleArguments - Sets the arguments for the localizable title key.
- urlArgs - Sets the value of the 'url-args' key from the submitted payload.

The `criteriaBuilder` object consists of 

- aliases - Sets a list of 'identifiers', like username or email address.
- variants - A filter for notifying only specific mobile variants of the Push Application.
- categories - A list of Categories. A Category is a semantical tag.
- deviceType - A filter for notifying only users running a certain device, e.g "iPad".

The `configBuilder` object consists of 

- timeToLive - Specify the Time To Live of the message, used by the APNs/FCM Push Networks. If the device is offline for a longer time than the ttl value, the supported Push Networks may not deliver the message to the client.

This is simple example of a UnifiedMessage

```Java
UnifiedMessage unifiedMessage = UnifiedMessage.withMessage()
  .alert("Hello")
  .sound("default")
  .criteria()
  .variants("c3f0a94f-48de-4b77-a08e-68114460857e") //e.g HR_Premium 
  .aliases("Peter", "Paula")
  .categories("sport", "Rugby")
  .deviceType("iPhone", "AndroidTablet")
  .build();

```

	

### Node Sender

The _UnifiedPush Server_ supports a Node Sender API. This version of the sender is compatible with the 1.1x series of the UnifiedPush Server. 

#### Getting Started

The sender dependency must be added to your project, this can be done by:

`npm i unifiedpush-node-sender`

#### Usage

The sender is created in your application with the following, where `url`, `applicationId`, `masterSecret` are values from your _UnifiedPush Server_

```Javascript
const agSender = require('unifiedpush-node-sender');

const settings = {
    url: '<yourPushServerURL e.g http://localhost:9999/>',
    applicationId: '<yourPushApplicationId e.g. B868CC08-BCC8-4A0A-B21E-1AC56AF0C734>',
    masterSecret: '<yourMasterSecret e.g. 4L30AV41-3278-4983-8F99-0EEA138J7O1I>'
};
```

Sending a message involves first, getting a handle on the client object and using `client.sender.send` method to send a single message. 

```Javascript
agSender(settings).then((client) => {
    client.sender.send(message, options).then((response) => {
        console.log('success', response);
    })
});
```
This is an example of what sending a single message would look like:

```Javascript
const agSender = require('./');

const settings = require('./settings.json');

const message = {
    alert: 'Hello World!',
    sound: 'alarm',
    badge: '1',
    userData: {
        someKey: 'a value',
        anotherCustomKey: 'another value'
    }
};

const options = {
    config: {
        ttl: 3600,
    },
    criteria: {
        variants: ['1234', '5678'],
        categories: ['category1', 'category3', 'category5']
    }
};


agSender(settings).then((client) => {
    client.sender.send(message, options).then((response) => {
        console.log('success', response);
    })
});
```


Sending multiple messages is a similar process but instead uses the `sendBatch` method
to pass an array of `{message, options}` objects.

```Javascript
agSender(settings).then((client) => {
    client.sender.sendBatch(messages).then((response) => {
        console.log('success', response);
    })
});
```

#### API Documentation

The Sender class returns a promise with the `client` object which contains a `settings` object for the particular client. The Sender settings consists of the:

- `url`  String - The URL of the Unified Push Server.
- `applicationId` String - The ID of an Application from the Unified Push Sever.
- `masterSecret` String - The master secret for that Application.
- `headers` Object - The hash of custom HTTP headers/ header overrides .


The `message` object that gets sent is made up of the following parameters:

- `alert` String - message that will be displayed on the alert UI element.
- `priority` String - sets a processing priority on a push message, values can be 'NORMAL' or 'HIGH'.
- `sound` String - The name of a sound file.
- `badge` String - The Number to display as the badge of the app icon.
- `userData` Object - any extra user data to be passed. 

For Applications on Apple devices/products there is a `message.apns` object

- `title` String - Describes the purpose of the notification.
- `action` String - The label of the action button.
- `urlArgs` Array - An array of values that are paired with the placeholders inside the url FormatString value of your website.json file. Safari Only.
- `titleLockey` String - Only for iOS, contains the key to a title string in the `Localizable.strings` file for the current localization. 
- `titleLocArgs` Array - Only for iOS, variable strings values appear in place of the format specifiers in `title-loc-key`.
- `actionCategory` String - The Identifier of the action category for the interactive notification.
- `contentAvailable` Boolean - Only for iOS, provide this key with a value of 1 to indicate that new content is available.

The `options` Object is made up of two sub parts, the `options.config` object and the `options.criteria` object

- `options.config` 
    - `ttl` Number - The time to live in second. This value is supported by APNS and FCM only. 

- `options.criteria` 
    - `alias` Array - A list of email or name strings.
    - `deviceType` Array - A list of device types as strings.
    - `categories` Array - A list of categories as strings.
    - `variants` Array - A list of variantID's as strings.

#### Response Codes

Two different response messages can be expected

A `202 message` will be received on a successful connection to the _UnifiedPush Server_ 

A `401 message` will be received on an authorization failure between the caller and the _UnifiedPush Server_. This would indicate that the `variantId` or the `masterSecret` are incorrect.


### REST Sender 

The _UnifiedPush Server_ supports a RESTful endpoint found at `/rest/send`. For connecting to RESTful clients this endpoint consumes and produces _JSON_ serialised messages that are protected by HTTP Basic authentication, returning a _202 Accepted_ status for message acceptance and a _401 Unauthorized_ status when there is an authentication error between the server and application.

#### Authentication
The _UPS RESTful_ endpoint receives a _HTTP POST_ request and checks the _HTTP Basic header_ to extract the  **pushApplicationId** and the **masterSecret** which make up a token in custom header and are sent as part of the application message for authentication against the push client.

This can be seen in an example `curl` request 
    
```Bash
[user@localhost ~}$ curl -u "PushApplicationID:MasterSecret"
  -v -H "Accept: application/json" -H "Content-type: application/json"
  -X POST
  -d '{
   "message": {
     "alert": "HELLO!",
     "sound": "default",
     "user-data": {
         "key": "value"
         }
        }
    }'
        https://SERVER:PORT/CONTEXT/rest/sender
```
       
#### Data Format 
The sent message is a JSON formatted instance of [`org.jboss.aerogear.unifiedpush.message`](https://github.com/aerogear/aerogear-unifiedpush-server/blob/master/model/push/src/main/java/org/jboss/aerogear/unifiedpush/message/Message.java). This message is made up of three different collections of information:

 - The Message 

    The format of message is made up of seven further parts, these are reserved keywords and Apple specific hooks will be invoked on the device, for Android the JSON map is submitted as is directly to the device. 

    - alert - Triggers a dialog, displaying the value - no iOS API needs to be invoked by the app developer.
    - sound - Plays a given sound - no iOS API needs to be invoked by the app developer.
    - badge - Sets the value of the badge icon - no iOS API needs to be invoked by the app developer.
    - priority - Priority enum values, NORMAL, HIGH.
    - user-data - Returns a map, representing any other key-value pairs that were sent to the RESTful sender API, this map usually contains application specific payload.

```JSON
"message" : {
  "alert": "Hello!",
  "sound": "default",
  "badge": 7,
  "priority": "NORMAL",
  "user-data": {
      "someKey": "some value",
      "anotherCustomKey": "another value"
      },
    },
```
    
- The Criteria
    
    This class contains the "query criteria" options for a message sent to the Send-HTTP endpoint, the following are the currently supported query criteria

    - aliases - A list of one or more identifiers, such as email or username. Alias needs to be stored when device is registering itself with the server.
    - deviceTypes - A list of raw device types that should receive the message, eg a coupon for Android users only. deviceType needs to be stored when the device is registering itself with the server.
    - categories - Helps to tag the current client with multiple categories. Gives semantic meaning to a registered installation.
    - variants - A list of one or more mobile variant ID's to identify a particular PushApplication variant, eg HR Android, HR iPad.

```JSON
"criteria": {
  "variants" : ["c3f0a94f-48de-4b77-a08e-68114460857e","444939cd-ae63-4ce1-96a4-de74b77e3737"],"alias" : ["user@account.com", "person@aerogear.org"],
  "deviceType": ["iPhone","AndroidTablet",],
  "categories" : ["firstCategory", "differentCategory"]
  },    
```
   
- The Config 
    
    The config class contains the `timeToLive` value for the message.

    - `ttl` - Specifies in seconds the `time-to-live` for the submitted notification, This value is supported by APNS and FCM. If a device is offline for longer than specified by the ttl value, the supported Push Networks may not deliver the notification.

```JSON
  "config": {
    "ttl" : 3600,
    }
```
#### Response Codes

Two different response messages can be expected.

- 202 message is received on a successful connection between the application and the sender endpoint.


- A 401 message is received on an authorization failure between the application and the sender endpoint,   this indicates that the `variantId` or the `masterSecret` are incorrect. The expected response header is:

    `header("WWW-Authenticate", "Basic realm=\"AeroGear UnifiedPush Server\"")`
