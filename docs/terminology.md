# Useful Terminology

Before we get into details, it’s important that we have a good lexicon.

## PushApplication

A logical construct that represents an overall mobile application (e.g. Mobile HR).

## Variant

A variant of the PushApplication, representing a specific mobile platform, like iOS or Android, 
or even more fine grain differentiation like iPad or iPhone. There can be multiple variants 
for a single PushApplication (e.g. HR Android, HR iPad, HR iPhone free or HR iPhone premium).

## Installation

Represents an actual device, registered with the UnifiedPush Server. User1 running HR Android app, 
while User2 runs HR iPhone premium on his phone.

## Push Notification Message

A simple message to be sent to a PushApplication.

## Sender Endpoint API

A RESTful API that receives Push Notification Message requests for a PushApplication or some of its 
different Variants. The Server translate this request into the platform specific details and delivers 
the payload to the 3rd party cloud provides, which eventually might deliver the message to the 
physical device.
