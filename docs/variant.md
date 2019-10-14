# Variant

## Android

// TODO Android library

| **Metadata**  | **Description**                                                     |
|---------------|---------------------------------------------------------------------|
| applicationID | The ID of the application where the variant is going to be created  |
| url           | The URL of your UnifiedPush server instance                         |
| port          | The HTTP port of your UnifiedPush server instance                   |
| name          | The name of the variant                                             |
| variantID     | (optional) The ID (string) you want to use for your new variant     |
| secret        | (optional) The secret (string) you want to use for your new variant |
| type          | Should be fixed: "android"                                          |
| googleKey     | FCM Server Key                                                      |
| projectNumber | FCM Sender ID                                                       |

```bash
curl \
-X POST $URL:$PORT/rest/applications/$APPLICATION_ID/android \
-H "Accept: application/json" \
-H "Content-Type: application/json" \
-d @- << EOF
{
    "name": "$ANDROID_VARIANT_NAME",
    "variantID": "$ANDROID_VARIANT_ID",
    "secret": "$ANDROID_VARIANT_SECRET",
    "type": "android",
    "googleKey": "$FCM_SERVER_KEY",
    "projectNumber": "$FCM_SENDER_ID"
}
EOF
```

## iOS

// TODO iOS library

| **Metadata**  | **Description**                                                     |
|---------------|---------------------------------------------------------------------|
| applicationID | The ID of the application where the variant is going to be created  |
| url           | The URL of your UnifiedPush server instance                         |
| port          | The HTTP port of your UnifiedPush server instance                   |
| name          | The name of the variant                                             |
| variantID     | (optional) The ID (string) you want to use for your new variant     |
| secret        | (optional) The secret (string) you want to use for your new variant |
| type          | Should be fixed: "ios"                                              |
| production    | If your certificate is for production                               |
| passphrase    | Your certificate passphrase                                         |
| certificate   | Full path to your certificate                                       |

```bash
curl \
-X POST $URL:$PORT/rest/applications/$APPLICATION_ID/ios \
-H "Accept: application/json" \
-H "Content-Type: application/json" \
-F "name=$IOS_VARIANT_NAME" \
-F "variantID=$IOS_VARIANT_ID" \
-F "secret=$IOS_VARIANT_SECRET" \
-F "type=ios" \
-F "production=false" \
-F "passphrase=$IOS_CERT_PASSPHRASE" \
-F "certificate=@$IOS_CERT_PATH"
```

## WebPush

// TODO WebPush library

| **Metadata**  | **Description**                                                     |
|---------------|---------------------------------------------------------------------|
| applicationID | The ID of the application where the variant is going to be created  |
| url           | The URL of your UnifiedPush server instance                         |
| port          | The HTTP port of your UnifiedPush server instance                   |
| name          | The name of the variant                                             |
| variantID     | (optional) The ID (string) you want to use for your new variant     |
| secret        | (optional) The secret (string) you want to use for your new variant |
| type          | Should be fixed: "web_push"                                         |
| publicKey     | // TODO                                                             |
| privateKey    | // TODO                                                             |
| alias         | // TODO                                                             |

```bash
curl  \
-X POST $URL:$PORT/rest/applications/$APPLICATION_ID/webpush \
-H "Accept: application/json" \
-H "Content-Type: application/json" \
-d @- << EOF
{
    "name": "WebPush",
    "variantID": "$WEBPUSH_VARIANT_ID",
    "secret": "$WEBPUSH_VARIANT_SECRENT",
    "type": "web_push",
    "publicKey": "$WEBPUSH_PUBLIC_KEY",
    "privateKey": "$WEBPUSH_PRIVATE_KEY",
    "alias": "$WEBPUSH_ALIAS"
}
EOF
```

// TODO Explain VAPID

?> ℹ️ [Secure VAPID key generator](https://tools.reactpwa.com/vapid)


