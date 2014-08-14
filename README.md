# AeroGear UnifiedPush Server [![Build Status](https://travis-ci.org/aerogear/aerogear-unifiedpush-server.png)](https://travis-ci.org/aerogear/aerogear-unifiedpush-server)

The _AeroGear UnifiedPush Server_ is a server that allows sending push notifications to different (mobile) platforms. The initial version of the server supports [Apple’s APNs](http://developer.apple.com/library/mac/#documentation/NetworkingInternet/Conceptual/RemoteNotificationsPG/Chapters/ApplePushService.html#//apple_ref/doc/uid/TP40008194-CH100-SW9), [Google Cloud Messaging](http://developer.android.com/google/gcm/index.html) and [Mozilla’s SimplePush](https://wiki.mozilla.org/WebAPI/SimplePush).

<img src="http://people.apache.org/~matzew/UPS_UI.png" height="303px" width="510px" />


## Getting started

Only three steps are needed to get going!

* Get the [latest WAR files](http://aerogear.org/push/)
* Setup a database of [your choice](http://aerogear.org/docs/unifiedpush/ups_userguide/server-installation/#_database_configuration)
* Start the {Wildfly|JBossAS7} server (e.g. ``$JBOSS/bin/standalone.sh -b 0.0.0.0``)
* Deploy the two `WAR` files to the [server](http://aerogear.org/docs/unifiedpush/ups_userguide/server-installation/#_deploy_the_unifiedpush_server)

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


## Development 

The above `Getting started` section covers the latest release of the UnifiedPush Server. For development and deploying `SNAPSHOT` versions, you will find infos in this section.


### Deployment 

For deployment to a specific server (Wildfly or JBossAS7), you need to build the WAR files and deploy them to a running and configured server.

First build the entire project:
```
mvn clean install
```

Note, this will build the also the WAR files for both, WildFly and JBossAS7. If you are only intereted in building for a specific platform, you can also use the profiles, discussed below.

#### Deployment to WildFly

For WildFly, invoke the following commands afer the build has been completed. This will deploy both WAR files to a running and configured Wildfly server.

```
cd servers
mvn wildfly:deploy -Pwildfly
```

#### Deployment to JBossAS7

For JBossAS7, invoke the following commands afer the build has been completed. This will deploy both WAR files to a running and configured AS7 server.

```
cd servers
mvn jboss-as:deploy -Pas7
```

### AdminUI and its release

The sources for administration console UI are placed under `admin-ui`.

For a build of the `admin-ui` during release, you can just run a Maven build, the `admin-ui` will be compiled by `frontend-maven-plugin` during `admin-ui` module build.

For instructions how to develop `admin-ui`, refer to [`admin-ui/README.md`](https://github.com/aerogear/aerogear-unifiedpush-server/blob/master/admin-ui/README.md).

These instructions contains also specific instructions how to upgrade NPM package dependencies.


## Openshift

For our Openshift Online cartridge we enforce HTTPS. This is done with a specific Maven Profile. To build the `WAR` files for Openshift the following needs to be invoked:

```
mvn clean install -Popenshift,test
```

The WAR file can be used to update our [Cartridge](https://github.com/aerogear/openshift-origin-cartridge-aerogear-push).

### Any questions ?

Join our [mailing list](https://lists.jboss.org/mailman/listinfo/aerogear-dev) for any questions and help! We really hope you enjoy our UnifiedPush Server!
