---
id: push-notifications
title: Push Notifications & In App Messaging
---

## Push Notifications
A push notification _is a message that is "pushed" from backend server or application to user interface, e.g. 
(but not limited to) mobile applications and desktop applications_(see [wikipedia](https://en.wikipedia.org/wiki/Push_technology#Push_notification)).

Push notification main advantage is to keep user interest high and avoid your application getting forgotten and uninstalled 
by allowing  you to send unsolicited messages (notifications) to the users that have your app installed.

:::warning
Push notifications are great marketing tool, but users could mute you if you abuse them.
:::

### _Push notification_ vs _text messages_

One question that could arise could be: _"Ok, if push notifications are a way to send unsolicited messages to the user, 
what is the difference with text messages?"_

Although they are similar in many ways, they have some important difference:
* you don't need to know the phone number of the user to send push notifications
* unsolicited SMS messages are illegal in some [state](https://www.consumer.ftc.gov/articles/how-recognize-and-report-spam-text-messages)
* _Push Notifications_ are strictly related to your application, while text messages aren't. When the user taps on a push 
  notification, your app will open. When the user taps on a text message, the messaging app will open 
* the user can choose to enable or disable _push notification_ from a particular app. That's not true for text messages 
  (user have to **block** you to stop receiving your messages)

### What compose a _push notifications_

A _push notification_ is composed by a number of fields. The most important are:
* **Title**: a title associated with the content of the notification
* **Icon**: an icon to be shown together with the content
* **Text**: the content of the notification

## In-App Messaging
The difference between a _push notification_ and an _in-app message_ is that the former is received when the application 
is closed or is running in background, while the latter is received only when the application is open and the user is 
actually using it.
For that reason, while _push notifications_ can be muted, the users can't mute an _in-app message_. And it will be unlikely
that he is bothered by the message since he is already using your application when the message is received. 