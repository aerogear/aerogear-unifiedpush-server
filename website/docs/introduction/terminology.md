---
id: ups-terminology
title: Useful Terminology
---

## PushApplication
A logical construct that represents an overall application (e.g. Android, Web App, etc).

## Variant
A variant of the _PushApplication_, representing a specific mobile platform, like _iOS_ or _Android_, or even more fine grain 
differentiation like _iPad_ or _iPhone_. There can be multiple variants for a single _PushApplication_ (e.g. Android, 
iPad, iPhone free or iPhone premium). Each supported variant type contains some platform specific properties, 
such as a Google API key (_Android_) or passphrase and certificate (_Apple_).

## Installation
Represents an actual device, registered with the _UnifiedPush Server_. User1 running **Android** app, while User2 runs 
**iPhone premium** on his phone.

## Push Notification Message
A simple message to be sent to a _PushApplication_.

## Sender Endpoint API
A _RESTful API_ that receives _Push Notification Message_ requests for a _PushApplication_ or some of its different _Variants_.
The Server translate this request into the platform specific details and delivers the payload to the 3rd party cloud
provides, which eventually might deliver the message to the physical device.