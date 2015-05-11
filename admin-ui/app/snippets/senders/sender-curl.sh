curl -u "{{ app.pushApplicationID }}:{{ app.masterSecret }}"  \
   -v -H "Accept: application/json" -H "Content-type: application/json"  \
   -X POST  -d \
  '{
     "message": {
      "alert": "Hello from the curl HTTP Sender!",
      "sound": "default"
     }
   }'  \
   {{ contextPath }}rest/sender
