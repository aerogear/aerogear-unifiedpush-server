# AeroGear UnifiedPush Server

[![Build Status](https://travis-ci.org/aerogear/aerogear-unifiedpush-server.png)](https://travis-ci.org/aerogear/aerogear-unifiedpush-server)
[![License](https://img.shields.io/:license-Apache2-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.jboss.aerogear.unifiedpush/unifiedpush-parent/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.jboss.aerogear.unifiedpush/unifiedpush-parent)

The _AeroGear UnifiedPush Server_ is a server that allows sending push notifications to different (mobile) platforms and has support for:
* [Apple’s APNs](http://developer.apple.com/library/mac/#documentation/NetworkingInternet/Conceptual/RemoteNotificationsPG/Chapters/ApplePushService.html#//apple_ref/doc/uid/TP40008194-CH100-SW9)
* [Firebase Cloud Messaging](https://firebase.google.com/docs/cloud-messaging/)
* [Microsoft's Windows Push Notification service (WNS)](https://msdn.microsoft.com/en-us/library/windows/apps/hh913756.aspx)
* [Microsoft's Push Notification service (MPNs)](http://msdn.microsoft.com/en-us/library/windows/apps/ff402558.aspx)
* [Amazon Device Messaging (ADM)](https://developer.amazon.com/appsandservices/apis/engage/device-messaging/) (*experimental*)
* [Mozilla’s SimplePush](https://wiki.mozilla.org/WebAPI/SimplePush) (_deprecated)_

<img src="https://raw.githubusercontent.com/aerogear/aerogear-unifiedpush-server/master/ups-ui-screenshot.png" height="427px" width="550px" />

## Project Info

|                 | Project Info  |
| --------------- | ------------- |
| License:        | Apache License, Version 2.0  |
| Build:          | Maven  |
| Documentation:  | https://aerogear.org/push/  |
| Issue tracker:  | https://issues.jboss.org/browse/AGPUSH  |
| Mailing lists:  | [aerogear-users](http://aerogear-users.1116366.n5.nabble.com/) ([subscribe](https://lists.jboss.org/mailman/listinfo/aerogear-users))  |
|                 | [aerogear-dev](http://aerogear-dev.1069024.n5.nabble.com/) ([subscribe](https://lists.jboss.org/mailman/listinfo/aerogear-dev))  |

## Getting started

Or for the on-premise version, execute the following steps to get going!

* Get the [latest WAR files](http://aerogear.org/push/)
* Setup a database of [your choice](https://aerogear.org/docs/unifiedpush/ups_userguide/index/#gendbds)
* Start the {Wildfly-10|EAP 7} server (e.g. ``$SERVER_HOME/bin/standalone.sh -c standalone-full.xml -b 0.0.0.0``)
* Deploy the two `WAR` files to the [server](https://aerogear.org/docs/unifiedpush/ups_userguide/index/#deploy)

Now go to ``http://localhost:8080/ag-push`` and enjoy the UnifiedPush Server.
__NOTE:__ the default user/password is ```admin```:```123```

#### Getting Started with Clustered Servers

In order to test on a cluster of WildFly servers, the default configuration serves pretty well, you just need to change startup script a bit - in following scenario we will use servers colocated on one node with configured port-offset:

    ./bin/standalone.sh -c standalone-full-ha.xml -Djboss.node.name=node1 -Djboss.messaging.cluster.password=somepassword -Djboss.socket.binding.port-offset=100 -Djava.net.preferIPv4Stack=true

And in a second terminal:

    ./bin/standalone.sh -c standalone-full-ha.xml -Djboss.node.name=node2 -Djboss.messaging.cluster.password=somepassword -Djboss.socket.binding.port-offset=200 -Djava.net.preferIPv4Stack=true

Note: on OS X, you need to enable multicast first:

    # Adds a multicast route for 224.0.0.1-231.255.255.254
    sudo route add -net 224.0.0.0/5 127.0.0.1

## Docker-Compose

For your convenience, we do have an easy way of launch with our [Docker compose file](docker-compose)

## Documentation

For more details about the current release, please consult [our documentation](https://aerogear.org/getstarted/guides/#push).

#### Generate REST Documentation

Up to date generated REST endpoint documentation can be found in `jaxrs/target/miredot/index.html`. It is generated with every `jaxrs` module build.

## Who is using it?

We have a list of users in our [wiki](https://github.com/aerogear/aerogear-unifiedpush-server/wiki/Users-of-the-UnifiedPush-Server). If you are using the UnifiedPush Server, please add yourself to the list!

## Development

The above `Getting started` section covers the latest release of the UnifiedPush Server. For development and deploying `SNAPSHOT` versions, you will find information in this section.


### Deployment

For deployment of the `master branch` to a specific server (Wildfly-10 or EAP7), you need to build the WAR files and deploy them to a running and configured server.

First build the entire project:
```
mvn clean install
```

Note, this will build the also the WAR files for both, WildFly-10 and EAP7.

#### Deployment to WildFly-10/EAP7

For WildFly, invoke the following commands afer the build has been completed. This will deploy both WAR files to a running and configured Wildfly server.

```
cd servers
mvn wildfly:deploy -Pwildfly
```

### AdminUI and its release

The sources for administration console UI are placed under `admin-ui`.

For a build of the `admin-ui` during release, you can just run a Maven build, the `admin-ui` will be compiled by `frontend-maven-plugin` during `admin-ui` module build.

For instructions how to develop `admin-ui`, refer to [`admin-ui/README.md`](https://github.com/aerogear/aerogear-unifiedpush-server/blob/master/admin-ui/README.md).

These instructions contains also specific instructions how to upgrade NPM package dependencies.

Note that the {{frontend-maven-plugin}} may fail if you killed the build during its work - it may leave the downloaded modules in inconsistent state, see [`admin-ui/README.md`](https://github.com/aerogear/aerogear-unifiedpush-server/blob/master/admin-ui/README.md#build-errors).

#### Cleaning the Admin UI build

In order to clean the state of Admin UI build caches, run maven build with the following parameter

    mvn clean install -Dfrontend.clean.force

Try this if the build fails e.g. after `bower.json` or `package.json` modifications to make sure no cache is playing with you.


## Releasing the UnifiedPush Server

The content of the [Release Process](https://github.com/aerogear/collateral/wiki/Release-Process-(Java)) is valid for this project as well. However, to build the `distribution` bundle, you need to include these profiles:

```
mvn release:GOAL -Pdist,test
```


## Deprecation Notices

###  1.1.0

*Chrome Packaged Apps*

The Chrome Packaged App Variant will be removed.  Google has deprecated the [chrome.pushMessaging API](https://developer.chrome.com/extensions/pushMessaging) in favor of the [chrome.gcm API](https://developer.chrome.com/extensions/gcm).

This change allows the UnifiedPush Server to now use the Android Variant for both Android and Chrome Apps.

If you are using this functionality, please convert your applications to use the new API and recreate your variants.

## Contributing

If you would like to help develop AeroGear you can join our [developer's mailing list](https://lists.jboss.org/mailman/listinfo/aerogear-dev), join #aerogear on Freenode, or shout at us on Twitter @aerogears.

Also takes some time and skim the [contributor guide](http://aerogear.org/docs/guides/Contributing/)

## How to develop and run tests

There is a dedicated guide to running and developing tests in [TESTS.md](./TESTS.md)

## Questions?

Join our [user mailing list](https://lists.jboss.org/mailman/listinfo/aerogear-users) for any questions or help! We really hope you enjoy app development with AeroGear!

## Found a bug?

If you found a bug please create a ticket for us on [Jira](https://issues.jboss.org/browse/AGPUSH) with some steps to reproduce it.
