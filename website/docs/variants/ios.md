---
id: ios
title: iOS Variant
---


 The following step-by-step guide gives you an introduction on how to use the AeroGear UnifiedPush Server for sending 
 Push Notifications to your own iOS Apps. The guide assumes you have an Apple developer account already setup, and uses tools available in macOS.

 To be able to send _push notifications_ to iOS, an _iOS variant_ must be created. That variant can be an _iOS (Certificate) Variant_ or an _iOS (APNS Token) Variant, 
 depending on the way we are going to authenticate to the APNs: 

 * with an APNs Certificate
 * with an APNs Token

 The first one is the legacy one: with this method you will be required to have one certificate for each application and, furthermore,
 you need two distinct certificates for the production and the development environments.
 
 The second one is the new token based authentication, which makes authentication much simpler:
 
 * The same key can be used for both development and production environments
 * The same key can be used for all the apps registered into your developer account
 * The key does not expire
 
 In this manual we will describe how to use both the approaches.

## APNs Certificate authentication
 A couple of configuration steps needs to be performed on the _Apple developer site_ before we can create the actual variant in the _Unified Push Server_:
 * Create a **CSR** _(Certificate Signing Request)_: this will be used to ask apple to generate a _development_ and/or _production_ certificate
 * Create an **App ID and its SSL certificate**: this will be used to authenticate the application to the APNs

#### Certificate Signing Request
 First you need to submit a request for a new digital certificate, which is based on a public/private key. The 
 Certificate itself is acting as the public key, and when you request it, a private key is added to your **KeyChain** 
 tool. The Cerficate will be used later on, to request an SSL certificate for the Apple Push Network service, which 
 will allow the _AeroGear Push Server_ to send notification messages to it. Now the actual CSR (Certificate Signing 
 Request) is done using **KeyChain Access** found in the **Applications > Utilities** folder on your Mac. Once opened, in 
 the **KeyChain Access** menu, choose **Certificate Assistant > Request a Certificate** from a **Certificate Authority**:
 
 > ![Creating a CSR](assets/ios/CreateCert.png "Creating a CSR")
 
 Make sure that you have chosen "Saved to disk" to save the CSR locally; we will upload it later in the provisioning portal when 
 requesting the actual SSL cert. Going back to **KeyChain Access** you now see a new private key:
 
 > ![Store CSR into KeyChain](assets/ios/KeyChain-keys.png)
 
#### Apple App ID and SSL certificate for APNs
 Now that the CSR is generated, go to the [Provisioning Portal](https://developer.apple.com/account/overview.action) 
 and log in with your Apple developer account. Now, click on the **Identifiers** link in order to create a new App ID 
 (use the **PLUS** Icon on the right). 
 In the window that will appear, select **App IDs**
 
 > ![Store CSR into KeyChain](assets/ios/ChooseAppIDs.png)
 
 Click on _continue_. In the form that will appear:
  * select _iOS, tvOS, watchOS_, give the **App ID** a _descriptive_ name, like **My first AeroGear Push App**
  * select **Bundle ID Explicit** (generic **Wildcard App ID** does not work with Push)
  * In the _Bundle ID_ field enter **YOUR** bundle ID. This is similar to Java packages. In this example we choose 
    _org.aerogear.PushHelloWorld_, however you must use your own ID
  * Double check that the **Push Notifications** checkbox is selected in the list of _Capabilities_:
 
:::note 
The _Bundle ID_ has to match the one from the actual iOS application that you are building later in this guide. 
:::
 
 > ![Set App ID](assets/ios/SetAppId.png)

In the next screen confirm your new App ID, and double check that the _Push Notifications_ option is enabled. 
Afterwards click the _Register_ button! Then, click on on the newly created App ID, and ensure that the _Push Notifications_ capability
is enable and click on the _Configure_ button close to that.
The following screen will appear:

 > ![Configure Push](assets/ios/ConfigurePush.png)

Here we are asked to generate a _Development_ and a _Production_ certificate that will be used by the UnifiedPush server 
when contacting the Apple Push Notification service to send messages. If you plan to distribute your app in the 
App Store, you are required to generate a Production certificate. In this guide we will generate both 
Certificates and register them with the UnifiedPush server.

Let’s start first by generating the _Development Certificate_. Click the _Create Certificate_ button on the 
_Development SSL Certificate_ section:

 > ![Upload CSR](assets/ios/UploadCSR.png)

Choose the _Certificate Signing Request_ you generate before and click on _Continue_: that will sign your certificate 
request and will give you the chance to download your newly generated certificate.

By following the same steps as before, you can now generate a production certificate too.

Now, if you go to your identifier, the _Push Notifications_ capability will appear as below:

 > ![Push Notifications Capability with cert](assets/ios/PushNotificationsWithCerts.png)

Click on _Edit_:

 > ![Download Push Certificates](assets/ios/DownloadPushCertificates.png)

Download and install the certificate (double click on it and choose `login` as keychain)
Open the KeyChain an select the `login` keychain and the `My Certificates` category:

 > ![KeyChain](assets/ios/KeyChain.png)

Now you have to export this certificate/private key pair as **Personal Information Exchange (.p12)**. This file will 
be uploaded later on to the AeroGear Push Server enabling it to authorize itself for your development application on 
Apple Push Network Service and send messages to it.

 > ![Download P12](assets/ios/DownloadP12.png)

When exporting the file you need to assign a passphrase that will protect the private key. Make note of the passphrase, because later 
when uploading the private key to the _AeroGear Push Server_ you will need both the exported file and the passphrase.

Follow the same steps to download the _Production SSL Certificate_, the file is being downloaded as aps_production.cer. 
This file will be uploaded later on to the AeroGear Push Server enabling it to authorize itself for your production 
application on Apple Push Network Service and send messages to it.

### Creating the variant in the UnifiedPush Server
APNs is now configured and we are now ready to setup the _UnifiedPush Server_ to connect to APNs and send push messages.

In the Wizard after you create a PushApplication, click the **Add Variant** button and fill out the iOS option. 
You will want to use the certificate and passphrase you create earlier:

 > ![Add Variant](assets/ios/AddVariant.png)

Afterwards you will see some code snippets containing the **Variant ID** and **Secret** that you can use in your iOS 
application for registering the device when running your app:

 > ![Add Variant](assets/ios/Snippets.png)

:::note
Clicking on the appropriate tab, you can choose between Objective-C, Swift and Cordova snippet |
:::

## APNs Token Authentication
 To be able to authentication using _token authentication_ you will need to collect the following informations:
 * your **bundle id**: This is similar to Java packages. In this example we choose _org.aerogear.PushHelloWorld_, however you must use your own ID
 * your **team ID**
 * your **private key**
 * your **key id**

### The TEAM ID
 Your _Team ID_ can be obtained very easily form your _developer account_:
 * Enter your development account
 * Click on _Certificates, IDs & Profiles_
 
 > ![Team ID](assets/ios/team-id.png)

 Your _team id_ will be on the upper right corner, right below your name, next to your team name.

### Generating the private key and the Key ID

As for the [team id](#the-team-id), go to the _Certificates, IDs & Profiles_ page.
Once there click _Keys_ to go the _keys management page_ and click on the _plus sign_ at the right of the _keys_ header:

 > ![Team ID](assets/ios/keys.png)

That will lead to a form where you will be able to set a name for the _key_ and select the feature to enable.

:::important
Double check that **Apple Push Notifications service (APNs)** is enabled here!
::: 
 
 > ![Team ID](assets/ios/apns-key.png)

We are almost done: click on _Continue_ and then on _Register_. That will bring you to a screen similar to the one below. 

 > ![Team ID](assets/ios/key-details.png)

 Take note of the _Key ID_ and click on _Download_ to save the key somewhere, since we will need it to create the variant.
 
 :::warning
 Please, take care of the downloaded key, since, after you click on _download_ you won't be able to download it again!
 :::
  
### Creating the variant in the UnifiedPush Server
APNs is now configured and we are now ready to setup the _UnifiedPush Server_ to connect to APNs and send push messages.

In the Wizard after you create a PushApplication, click the **Add Variant** button and select **iOS (APNS Token)**. 
You will need the private key you just downloaded, its **key id**, your **team id** and your **bundle id**

 ![Add Variant](assets/ios/apns-token-variant.png)

Afterwards you will see some code snippets containing the **Variant ID** and **Secret** that you can use in your iOS 
application for registering the device when running your app:

 > ![Add Variant](assets/ios/apns-token-variant-details.png)

:::note
Clicking on the appropriate tab, you can choose between Objective-C, Swift and Cordova snippet |
:::