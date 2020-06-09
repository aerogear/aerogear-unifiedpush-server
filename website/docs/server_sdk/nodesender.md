---
id: nodesender
title: Node Sender API
---


The _UnifiedPush Server_ supports a Node Sender API. This version of the sender is compatible with the 1.1x series of the UnifiedPush Server. 

## Getting Started

The sender dependency must be added to your project, this can be done by:

`npm i unifiedpush-node-sender`

## Usage

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

## API Documentation

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

## Response Codes

Two different response messages can be expected

A `202 message` will be received on a successful connection to the _UnifiedPush Server_ 

A `401 message` will be received on an authorization failure between the caller and the _UnifiedPush Server_. This would indicate that the `variantId` or the `masterSecret` are incorrect.
