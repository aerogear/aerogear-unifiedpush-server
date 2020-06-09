---
id: push-notifications
title: Push Notifications
---

A push notification _is a message that is "pushed" from backend server or application to user interface, e.g. 
(but not limited to) mobile applications and desktop applications_(see [wikipedia](https://en.wikipedia.org/wiki/Push_technology#Push_notification)).

Push notifications allow your services to signal to you application there there is work it needs to do. This can 
include background work like synchronizing data or user visible work like displaying relevant. Push messages can be 
used to keep your application fresh and relevant so it isn't forgotten and uninstalled.

:::warning
Push notifications are great marketing tool, but you could lose users if you abuse them to send spam notifications.
:::

## _Push notification_ vs _text messages_

One question that could arise could be: _"Ok, if push notifications are a way to send unsolicited messages to the user, 
what is the difference with text messages?"_

Although they are similar in many ways, they have some important difference:
* you don't need to know the phone number of the user to send push notifications
* unsolicited SMS messages are illegal in some [state](https://www.consumer.ftc.gov/articles/how-recognize-and-report-spam-text-messages)
* _Push Notifications_ are strictly related to your application, while text messages aren't. When the user taps on a push 
  notification, your app will open. When the user taps on a text message, the messaging app will open 
* the user can choose to enable or disable _push notification_ from a particular app. That's not true for text messages 
  (user have to **block** you to stop receiving your messages)
* _Push Notifications_ can be used to trigger some work in your application, while text messages can't

### What compose a _push notifications_

A _push notification_ is composed by a number of fields. The most important are:
* **Title**: a title associated with the content of the notification
* **Icon**: an icon to be shown together with the content
* **Text**: the content of the notification

## _Push to Sync_ notifications

When a push notification is received, you are not required to show it to the user. 
A message could notify the application that something new is ready and, for example, the application should sync as soon as possible.

To avoid draining the battery, your _notification handler_ would schedule data synchronization with the server using 
your device OS's work scheduler.

## Push notifications and battery draining

The operating system manages receiving push notification and batches work with other applications. By using push 
messaging as a signal your OS can schedule your application to do work when it is least impactful on the battery. 