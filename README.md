AeroGear PushEE
===============

AeroGear's Connectivity Server (Java EE poc)

### Some guidance 

#### Register Push App

Register a ```PushApplication```, like _Mobile HR_:

```
curl -v -H "Accept: application/json" -H "Content-type: application/json" -X POST -d '{"name" : "MyApp", "description" :  "awesome app" }' http://localhost:8080/ag-push/rest/applications
```

_The response returns an ID for the Push App...._

##### iOS Variant

Add an ```iOS``` variant (e.g. _HR for iOS_):
```
curl -i -H "Accept: application/json" -H "Content-type: multipart/form-data" 

  -F "certificate=@/Users/matzew/Desktop/MyCert.p12"
  -F "passphrase=TopSecret"

  -X POST http://localhost:8080/ag-push/rest/applications/{PUSH_ID}/iOS
```

**NOTE:** The above is a _multipart/form-data_, since it is required to upload the "Apple Push certificate"!

_The response returns an ID for the iOS variant...._

##### Android Variant

Add an ```android``` variant (e.g. _HR for Android_):
```
curl -v -H "Accept: application/json" -H "Content-type: application/json"
  -X POST
  -d '{"googleKey" : "IDDASDASDSA"}'
  
  http://localhost:8080/ag-push/rest/applications/{PUSH_ID}/android 
```

#### Registration of an installation, on a device (iOS)

Client-side example for how to register an installation:

```ObjectiveC
- (void)application:(UIApplication*)application
  didRegisterForRemoteNotificationsWithDeviceToken:(NSData*)deviceToken
{
    NSString *tokenStr = [deviceToken description];
    NSString *pushToken = [[[tokenStr
      stringByReplacingOccurrencesOfString:@"<" withString:@""]
      stringByReplacingOccurrencesOfString:@">" withString:@""]
      stringByReplacingOccurrencesOfString:@" " withString:@""];

  // TODO: use https
    AFHTTPClient *client =
    [[AFHTTPClient alloc] initWithBaseURL:
         [NSURL URLWithString:@"http://192.168.0.114:8080/ag-push/"]];
    client.parameterEncoding = AFJSONParameterEncoding;

    // set the AG headers....
    [client setDefaultHeader:@"ag-push-app" 
     value:@"SOME ID..."];
    [client setDefaultHeader:@"ag-mobile-app"
	   value:@"SOME OTHER ID..."];



    [client postPath:@"/registry/device"
	  parameters:
       @{
          @"deviceToken": pushToken,
          @"deviceType": @"iPhone", 
          @"mobileOperatingSystem": @"iOS", 
          @"osVersion": @"6.1.3"
        }
	  success:^(AFHTTPRequestOperation *operation, id responseObject) {
        NSLog(@"\nSUCCESS....\n");
    } failure:^(AFHTTPRequestOperation *operation, NSError *error) {
        NSLog(@"%@", error);
    }];
}
```
_No real client SDK, YET!!!!_

#### Registration of an installation, for an Android device:

CURL example for how to register an installation:

```
curl -v -H "Accept: application/json" -H "Content-type: application/json" 
   -H "ag-push-app: {id}"
   -H "ag-mobile-app: {id}"
   -X POST
   -d '{
      "deviceToken" : "someTokenString", 
      "deviceType" : "ANDROID", 
      "mobileOperatingSystem" : "android", 
      "osVersion" : "4.0.1"
    }'

http://localhost:8080/ag-push/rest/registry/device 
```


### Sender

Send broadcast push message to ALL mobile apps of a certain Push APP......:

```
curl -v -H "Accept: application/json" -H "Content-type: application/json" 
   -X POST

   -d '{"key":"blah", "alert":"HELLO!"}'
   
http://localhost:8080/ag-push/rest/sender/broadcast/{id} 
```

## More details

Concepts and ideas are also being developed...:

See:
https://gist.github.com/matzew/69d33a18d4fac9fdedd4

REST APIs

* Registry: https://gist.github.com/matzew/2da6fc349a4aaf629bce
* Sender: https://gist.github.com/matzew/b21c1404cc093825f0fb
