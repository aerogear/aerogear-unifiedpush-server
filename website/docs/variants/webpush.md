---
id: webpush
title: WebPush Variant
---


 The following step-by-step guide gives you an introduction on how to use the _AeroGear UnifiedPush Server_ for sending 
 _Web Push Notifications_ to your web application.
 
## Create WebPush Variants

 Before you can create a _WebPush Variant_ with the _UnifiedPush Server_ you need to get your VAPID Keys 
 (see [The Web Push Protocol](https://developers.google.com/web/fundamentals/push-notifications/web-push-protocol) for details).
 You can use, for example, [this site](https://tools.reactpwa.com/vapid) to generate them.

 As soon as you have the keys, open (or create) an UPS application and click on the `Add A Variant` button

 > ![Add Variant](assets/webpush/add_variant.png "Add Variant")

 A popup will open. Select `WebPush` and fill in the data:
 * Vapid Public Key: your vapid public key
 * Vapid Private Key: your vapid private key
 * Alias: The application server contact information (this must be a `mailto` or an `https` url)
 
 > ![WebPush Variant](assets/webpush/webpush_variant.png "WebPush Variant")

Click on `create` and your webpush variant is ready!