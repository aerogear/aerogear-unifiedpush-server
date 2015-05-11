curl -u "{{ app.pushApplicationID }}:{{ app.masterSecret }}"
   -v -H "Accept: application/json" -H "Content-type: application/json"
   -X POST
   -d '{
     "alias" : ["someUsername"],
     "deviceType" : ["someDevice"],
     "categories" : ["someCategories"],
     "variants" : ["someVariantIDs"],
     "ttl" : 3600,
     "message":
     {
       "key":"value",
       "key2":"other value",
       "alert":"HELLO!",
       "action-category":"some value",
       "sound":"default",
       "badge":2,
       "content-available" : true
     },
     "simple-push":"version=123"
   }'
   {{ contextPath }}rest/sender
