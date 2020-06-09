---
id: ups-overview
title: Overview
---

The _AeroGear UnifiedPush Server_ is a server that allows sending native push messages to different mobile and web applications. 

Currently the server supports:
* [Apple's APNS](https://developer.apple.com/go/?id=push-notifications)
* [Firebase Cloud Messaging](https://firebase.google.com/)
* [Web Push](https://developer.mozilla.org/en-US/docs/Web/API/Push_API)

By using the _AeroGear UnifiedPush Server_ you will get a unified _Notification Service API_ to the above mentioned _Push Network Services_.
When a push message request is sent to the UnifiedPush Server, it is internally translated into the format of these 3rd party networks. 
This gives a server the ability to send Push notifications to different mobile platforms.

:::note
When using the _UnifiedPush Server_, please keep in mind that Push notification is a signalling mechanism and that it is not suitable to be used as a data carrying system (e.g. use in a chat application).
:::