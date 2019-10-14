# Device registration

To receive native push notifications from a Push Network (e.g. APNs, FCM or WebPush), the mobile device 
is identified with a unique _device-token_, assigned by the actual Push Network. This device-token is passed, 
by the underlying Operating-System, to the mobile application.

The _device-token_ needs to be registered with the AeroGear UnifiedPush Server, 
to indicate there is a new Installation for a Variant.

## Registration and Updating

Everytime when a mobile application launches it receives the above mentioned device-token, 
via a platform-specific method (or callback). Since the Push Network may assign a new token to a device, 
it is **recommended** to always (re)register the device-token with the AeroGear UnifiedPush Server 
when application launch

### The required metadata for an Installation:

| **Metadata**  | **Description**                                           |
|---------------|-----------------------------------------------------------|
| deviceToken   | Identifies the device/user-agent within its Push Network. |
| variantID     | The ID of the variant, where the client belongs to        |
| variantSecret | Password of the actual variant                            |

### User specific metadata

| **Metadata**     | **Description**                                  |
|------------------|--------------------------------------------------|
| deviceType       | The device type of the device or the user agent. |
| operatingSystem  | The name of the underlying Operating System.     |
| osVersion        | The version of the used Operating System.        |
| alias            | Identify users with the system.                  |
| categories       | Used to apply one or more “tags”.                |

### Android

```bash

```

### iOS

### WebPush


// TODO how to register a device

```bash

```

## Unregistration

// TODO

### Example

// TODO how to unregister a device

```bash

```

