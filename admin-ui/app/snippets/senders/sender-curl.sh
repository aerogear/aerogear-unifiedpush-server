curl -u "{{ app.pushApplicationID }}:{{ app.masterSecret }}"
   -v -H "Accept: application/json" -H "Content-type: application/json"
   -X POST
   -d '{
     "message": {
      "alert": "HELLO!",
      "sound": "default",
      "badge": 2,
      "user-data": {
          "key": "value",
          "key2": "other value"
      },
      "windows": {
          "type": "tile",
          "duration": "short",
          "badge": "alert",
          "tileType": "TileWideBlockAndText01",
          "images": ["Assets/test.jpg", "Assets/background.png"],
          "textFields": ["foreground text"]
      },
      "apns": {
          "title" : "someTitle",
          "action-category": "some value",
          "content-available": true,
          "action" : "someAction",
          "url-args" :["args1","arg2"],
          "localized-title-key" : "some value",
          "localized-title-arguments" : ["args1","arg2"]
      }
      "simple-push": "version=123"
     },
     "criteria": {
         "alias": [ "someUsername" ],
         "deviceType": [ "someDevice" ],
         "categories": [ "someCategories" ],
         "variants": [ "someVariantIDs" ]
     },
     "config": {
         "ttl": 3600
     }
   }'
   {{ contextPath }}rest/sender
