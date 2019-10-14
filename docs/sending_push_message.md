# Sending Push Message

## Message Format

```bash
curl -u "{PushApplicationID}:{MasterSecret}"
   -v -H "Accept: application/json" -H "Content-type: application/json" 
   -X POST
   -d '{
       "criteria": {
         "variants" : ["c3f0a94f-48de-4b77-a08e-68114460857e", "444939cd-ae63-4ce1-96a4-de74b77e3737" ....],
         "alias" : ["user@account.com", "someone@aerogear.org", ....],
         "categories" : ["someCategory", "otherCategory"],
         "deviceType" : ["iPad", "AndroidTablet"]
       },
       "message": {
         "alert":"HELLO!",
         "sound":"default",
         "badge":7,
         "content-available" : true,
         "action-category" : "some_category",
         "simple-push": "version=123",
         "user-data": {
            "someKey":"some value",
            "anotherCustomKey":"some other value"
         },
         "windows": {                                                
            "type": "tile",                                         
            "duration": "short",                                    
            "badge": "alert",                                       
            "tileType": "TileWideBlockAndText01",                   
            "images": ["Assets/test.jpg", "Assets/background.png"], 
            "textFields": ["foreground text"]                       
          },                                                           
       },
       "config": {
         "ttl" : 3600,
       }
     }'
   https://SERVER:PORT/CONTEXT/rest/sender
```

| **Key**     | **Type** | **Description**                             |
|-------------|----------|---------------------------------------------|
| alert       | String   | The message itself                          |

## Libraries

// TODO Explain about the Sender libraries

### Java

* [Java](https://github.com/aerogear/aerogear-unifiedpush-java-client)

// TODO Add Java Sender library example code

### NodeJS

* [NodeJS](https://github.com/aerogear/aerogear-unifiedpush-nodejs-client)

// TODO Add Node Sender library example code
