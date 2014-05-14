# AeroGear UnifiedPush Server [![Build Status](https://travis-ci.org/aerogear/aerogear-unifiedpush-server.png)](https://travis-ci.org/aerogear/aerogear-unifiedpush-server)

The _AeroGear UnifiedPush Server_ is a server that allows sending push notifications to different (mobile) platforms. The initial version of the server supports [Apple’s APNs](http://developer.apple.com/library/mac/#documentation/NetworkingInternet/Conceptual/RemoteNotificationsPG/Chapters/ApplePushService.html#//apple_ref/doc/uid/TP40008194-CH100-SW9), [Google Cloud Messaging](http://developer.android.com/google/gcm/index.html) and [Mozilla’s SimplePush](https://wiki.mozilla.org/WebAPI/SimplePush). It also includes a web interface to configure your applications and push networks.


## Installation

Prerequisites: 
  - Java EE 7 SDK (http://www.oracle.com/technetwork/java/javaee/downloads/index.html)
  - Maven (http://maven.apache.org/)
  - npm (https://www.npmjs.org/)
  - WildFly (http://wildfly.org/downloads/)

These instructions assume you have the `wildfly` and `aerogear-unifiedpush-server` folders in the same parent location.


``` 
git clone https://github.com/aerogear/aerogear-unifiedpush-server.git
cd aerogear-unifiedpush-server/
mvn install
cd new-admin/
npm install
bower install
cp -R server/target/ag-push ../../wildfly/standalone/deployments/ag-push.war
cp databases/unifiedpush-h2-ds.xml ../../wildfly/standalone/deployments
touch ../../wildfly/standalone/deployments/ag-push.war.dodeploy
```

Edit `aerogear-unifiedpush-server/new-admin/local-config.json` and make sure `jbossweb` points to the right location.


## Running

```
./wildfly/bin/standalone.sh
cd aerogear-unifiedpush-server/new-admin/
grunt server
```

Open `localhost:8080/ag-push/` in your browser to access the administration interface.
You can log in temporarily with the user `admin` and password `123`. It is recommended you change the password immediately.


## Related documentation

### Configuration

For more detailed about configuration options, see the [README.extended.md](README.extended.md) file.

### Specifications

* [AeroGear UnifiedPush Server](http://aerogear.org/docs/specs/aerogear-server-push/)
* [Client Registration](http://aerogear.org/docs/specs/aerogear-client-push/)
* [Push Message Format](http://aerogear.org/docs/specs/aerogear-push-messages/)

### REST APIs

Documentation for the REST APIs of the AeroGear UnifiedPush Server can be found [here](http://aerogear.org/docs/specs/aerogear-push-rest/).
