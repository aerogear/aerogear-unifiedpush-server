# AeroGear UnifiedPush Server [![Build Status](https://travis-ci.org/aerogear/aerogear-unifiedpush-server.png)](https://travis-ci.org/aerogear/aerogear-unifiedpush-server)

The _AeroGear UnifiedPush Server_ is a server that allows sending push notifications to different (mobile) platforms. The initial version of the server supports [Apple’s APNs](http://developer.apple.com/library/mac/#documentation/NetworkingInternet/Conceptual/RemoteNotificationsPG/Chapters/ApplePushService.html#//apple_ref/doc/uid/TP40008194-CH100-SW9), [Google Cloud Messaging](http://developer.android.com/google/gcm/index.html) and [Mozilla’s SimplePush](https://wiki.mozilla.org/WebAPI/SimplePush).

<img src="http://people.apache.org/~matzew/UPS_UI.png" height="303px" width="510px" />


## Getting started

Only three steps are needed to get going!

* Setup the database by copying this [datasource XML file](https://github.com/aerogear/aerogear-unifiedpush-server/blob/0.10.x/databases/unifiedpush-h2-ds.xml) into ``$JBOSS/standalone/deployments``
* Start the Server (e.g. ``$JBOSS/bin/standalone.sh -b 0.0.0.0``)
* Go to the `servers` folder
* Run `mvn clean jboss-as:deploy` or `mvn -Pwildfly clean wildfly:deploy`

Now go to ``http://localhost:8080/ag-push`` and enjoy the UnifiedPush Server.
__NOTE:__ the default user/password is ```admin```:```123```

## Documentation

For more details about the current release, please consult [our documentation](http://aerogear.org/docs/unifiedpush/).

### Instructions for Keycloak administration console

Note: The instructions below are pretty much based on [Keycloak integration with UPS](https://github.com/keycloak/keycloak/blob/master/project-integrations/aerogear-ups/README.md).

* The aerogear security admin (keycloak) http://localhost:8080/auth/admin/aerogear/console/index.html
* The aerogear user account page (keycloak) http://localhost:8080/auth/realms/aerogear/account

### SSL by default

The Keycloak directives inside UnifiedPush server will enforce SSL to **all** external IP addresses, except for *localhost* and Docker images.


## Developing and releasing UI

The sources for administration console UI are placed under `admin-ui`.

For a build of the `admin-ui` during release, you can just run a Maven build, the `admin-ui` will be compiled by `frontend-maven-plugin` during `server` module build.

`admin-ui` has several node.js module and JS library dependencies. By default, the "$ mvn clean" command will delete all previously downloaded node.js related directories. 
If you want to make your build faster and not download same packages over again, please use profile intended for this purpose:

    mvn clean install -Pdev

For instructions how to develop `admin-ui`, refer to [`admin-ui/README.md`](https://github.com/aerogear/aerogear-unifiedpush-server/blob/master/admin-ui/README.md).

These instructions contains also specific instructions how to upgrade NPM package dependencies.


## Developing and Openshift

For our Openshift Online cartridge we enforce HTTPS. This is done with a specific Maven Profile. To build the `WAR` files for Openshift the following needs to be invoked:

```
mvn clean install -Popenshift,test
```

The WAR file can be used to update our [Cartridge](https://github.com/aerogear/openshift-origin-cartridge-aerogear-push).

### Any questions ?

Join our [mailing list](https://lists.jboss.org/mailman/listinfo/aerogear-dev) for any questions and help! We really hope you enjoy our UnifiedPush Server!
