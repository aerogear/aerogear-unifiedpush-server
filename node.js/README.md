Before running the test script, make sure to add a new client in the Keycloak Admin Console:
http://localhost:8080/auth/admin/aerogear/console/

The new client needs to be called `ups-client`, and make sure `Direct Grants Only` are _ON_. It is also _required_ to set the `Access Type` to `public`.

The other options are not relevant to execute the script. Once done, the script can be executed via:
```
node directgranttest.js
```

Afterwards a new `Push Application` should be visible in the UI.

__NOTE:__ The test/demo script LACKS the password for admin...
