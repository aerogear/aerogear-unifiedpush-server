---
id: how-it-works
title: How the UnifiedPush Server works
---

The _AeroGear UnifiedPush Server_ can be seen as a broker that distributes push messages to different **3rd party Push Networks**. 
The graphic below gives a little overview:

![UnifiedPushServer Overview](../../img/ups-overview.png)

1. One _PushApplication_ and at least one mobile platform variant must be created. Per platform documentation is in the **Configuring Variants** section of the sidebar.
2. The variant credentials that are generated and stored by the _UnifiedPush Server_ must be added to the mobile application 
source, enabling the application running on the mobile device to register with the _UnifiedPush Server_ once it is 
installed on mobile devices. The **Configuring Clients** section of the sidebar contains more details.
3. Sending a push message can happen in different ways:
    * Using the _UnifiedPush Admin UI_
    * Using the _Sender APIs_
    
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

## Privacy Note

As explained above, the payload of the push notification is delivered to 3rd party Push Network providers, like Google or Apple.

:::warning
It is highly recommended to not sent any send sensitive personal or confidential information belonging to an individual (e.g. a social security number, financial account or transactional information, or any information where the individual may have a reasonable expectation of secure transmission) as part of any Push Notification!
:::

For analytic purposes on our Dashboard we store the content of the alert key sent to the _UnifiedPush Server_. 
The content of the alert key belongs to the metadata, which is deleted after 30 days, using a nightly job within the _UnifiedPush Server_.

## Useful links

* [Google Firebase Cloud Messaging](https://firebase.google.com/products/cloud-messaging)
* [Apple APNs docs](https://developer.apple.com/library/archive/documentation/NetworkingInternet/Conceptual/RemoteNotificationsPG/APNSOverview.html)
