---
id: restfulsender
title: RESTful Sender API
---


The _UnifiedPush Server_ supports a RESTful endpoint found at `/rest/send`. For connecting to RESTful clients this endpoint consumes and produces _JSON_ serialised messages that are protected by HTTP Basic authentication, returning a _202 Accepted_ status for message acceptance and a _401 Unauthorized_ status when there is an authentication error between the server and application.

## Authentication
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
       
## Data Format 
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
## Response Codes

Two different response messages can be expected.

- 202 message is received on a successful connection between the application and the sender endpoint.


- A 401 message is received on an authorization failure between the application and the sender endpoint,   this indicates that the `variantId` or the `masterSecret` are incorrect. The expected response header is:

    `header("WWW-Authenticate", "Basic realm=\"AeroGear UnifiedPush Server\"")`
