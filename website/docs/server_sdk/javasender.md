---
id: javasender
title: Java Sender API
---


The _UnifiedPush Server_ supports a Java API Client for sending Push Notifications

## Getting Started

You must add the following dependencies to your `pom.xml` file:

```xml
    <dependency>
    <groupId>org.jbos.aerogear</groupId>
    <artifactId>unifiedpush-java-client</artifactIt>
    <version>1.1.0.Final</version>
    </dependency>
```

## Usage

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

## Sending Messages

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