The AeroGear UnifiedPush Server offers a unified Notification Service API to the above 
mentioned Push Network Services. When a push message request is sent to the 
UnifiedPush Server, it is internally translated into the format of these 3rd party 
networks. This gives a server the ability to send Push notifications to different 
mobile platforms has:

* [Apple Push Notification Service](https://developer.apple.com/notifications/)
* [Firebase Cloud Messaging](https://firebase.google.com/products/cloud-messaging/)

?> ℹ️ When using the UnifiedPush Server, please keep in mind that Push notification is a signalling 
mechanism and that it is not suitable to be used as a data carrying system 
(e.g. use in a chat application).

!> It is highly recommended to not sent any send sensitive personal or confidential information 
belonging to an individual (e.g. a social security number, financial account or transactional 
information, or any information where the individual may have a reasonable expectation of secure 
transmission) as part of any Push Notification!
