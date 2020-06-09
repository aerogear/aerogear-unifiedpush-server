---
id: agnigno
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

## How the UnifiedPush Server works
The _AeroGear UnifiedPush Server_ can be seen as a broker that distributes push messages to different 3rd party Push Networks. The graphic below gives a little overview:

![Load Firebase Assistant](/img/ups-overview.png) 

1. One PushApplication and at least one mobile platform variant must be created (see [Configuring Variants](/docs/configuring_variants)).
2. The variant credentials that are generated and stored by the _UnifiedPush Server_ must be added to the mobile application 
source, enabling the application running on the mobile device to register with the _UnifiedPush Server_ once it is 
installed on mobile devices (see [Configuring Clients](/docs/configuring_clients)).
3. Sending a push message can happen in different ways:
    * Using the _UnifiedPush Admin UI_
    * Using the _Sender APIs_However
    The former can be used to send test messages, while the latter should be used in a real-world scenarios where 
    the _Push Notification Message_ request is triggered from a backend application. 
    Different SDKs for different languages are supported.
4. The push request is then translated into platform specific details for the required variant Push Network.
The Dashboard of the AdminUI gives a status report if a message is sent to the Push Network.
5. The _UnifiedPush Server_ does not directly deliver the message to the mobile device. This is done by the appropriate 
variant Push Network. 

:::note
There can be latency in the actual delivery. Most Push Networks, such as APNs or FCM, do not guarantee to deliver messages to mobile devices.
:::

## Privacy note
As explained in the How the _UnifiedPush Server_ works section, the payload of the push notification is delivered to 3rd party Push Network providers, like Google or Apple.

:::warning
It is highly recommended to not sent any send sensitive personal or confidential information belonging to an individual (e.g. a social security number, financial account or transactional information, or any information where the individual may have a reasonable expectation of secure transmission) as part of any Push Notification!
:::

For analytic purposes on our Dashboard we store the content of the alert key sent to the _UnifiedPush Server_. 
The content of the alert key belongs to the metadata, which is deleted after 30 days, using a nightly job within the _UnifiedPush Server_.

## Use-cases and scenarios
Different use-cases and scenarios are supported. Below are a few to give an idea how the _UnifiedPush Server_ can be used:
* **MyWarehouseInc-backend** can send notification messages to different groups (e.g. discounts for only iOS (or only Android) users)
* **MyInsuranceCorp-backend** can send “notification messages” to different variants of its mobile Applications:
  * App for the Customers
  * App for the employed Sales Agents
* Publishing Company:
  * **MyPublishing-Company-backend** sends update “notification messages” to all of its apps (free and premium - regardless of the mobile OS)
  * **Targeting**: Sending push messages to different groups of users. For instance, availability of “advanced content” is only notified to the paying customers (e.g. those that run the premium app).
* A company has different backends (small/simple apps for different tasks) - and these different backends could be able to reach all (or some) of the company’s mobile apps.
