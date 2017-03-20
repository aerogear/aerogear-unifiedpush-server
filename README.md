# UnifiedPush Server

[![Build Status](https://travis-ci.org/aerobase/unifiedpush-server.svg?branch=master)](https://travis-ci.org/aerobase/unifiedpush-server)
[![License](https://img.shields.io/:license-Apache2-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0)

* [Apple’s APNs](http://developer.apple.com/library/mac/#documentation/NetworkingInternet/Conceptual/RemoteNotificationsPG/Chapters/ApplePushService.html#//apple_ref/doc/uid/TP40008194-CH100-SW9)
* [Firebase Cloud Messaging](https://firebase.google.com/docs/cloud-messaging/)
* [Microsoft's Windows Push Notification service (WNS)](https://msdn.microsoft.com/en-us/library/windows/apps/hh913756.aspx)
* [Microsoft's Push Notification service (MPNs)](http://msdn.microsoft.com/en-us/library/windows/apps/ff402558.aspx)
* [Amazon Device Messaging (ADM)](https://developer.amazon.com/appsandservices/apis/engage/device-messaging/) (*experimental*)
* [Mozilla’s SimplePush](https://wiki.mozilla.org/WebAPI/SimplePush) (_deprecated)_

_AeroBase UnifiedPush Server_ releases additional functionality while maintaining _AeroGear_ API compatibility:
* Collections DB - NoSQL Document Database, store & access your documents from both the server and the client.
* Scalable & Fault Tolerant, Based on apache cassandra database.
* By default, every AeroBase package comes with an embeded NGINX, Wildfly 10.1 & KeyCloak 2.4, Cassandra 3.9.
* OTP Registraion - [SMS/Email Verification process](https://github.com/aerobase/unifiedpush-server/issues/2).
* SSL Support and easy configuration.
* Silent Push Notifications (Notification without payload).
* Centralized configuration/managment using Chef Omnibus.
* [Full-stack](http://ups.c-b4.com/ups/packages/) rpm/deb installers across a variety of platforms (RHEL, Debian, Fedora, Ubuntu).

<img src="https://raw.githubusercontent.com/aerobase/unifiedpush-server/master/ups-ui-screenshot.png" height="427px" width="550px" />
<img src="https://raw.githubusercontent.com/aerobase/unifiedpush-server/master/ups-home-ui-screenshot.png" height="427px" width="550px" />

## Project Info

|                 | Project Info  |
| --------------- | ------------- |
| License:        | Apache License, Version 2.0  |
| Build:          | Maven  |
| Documentation:  | https://aerogear.org/push/  |
|                 | https://github.com/aerobase/omnibus-unifiedpush-server/tree/master/doc  |
| Issue tracker:  | https://github.com/aerobase/unifiedpush-server/issues  |

## Getting started

Or for the on-premise version, execute the following steps to get going!

* Download the [latest package (rpm/deb) files](http://ups.c-b4.com/ups/packages/)
* Or follow the steps on the [install page](https://github.com/aerobase/unifiedpush-server/wiki/Unifiedpush-Installation)
* Run ``sudo unifiedpush-server reconfigure``
* Start the server ``sudo unifiedpush-server start``

Now go to ``http://localhost/unifiedpush-server`` and enjoy the UnifiedPush Server.
__NOTE:__ the default user/password is ```admin```:```123```

#### Getting Started with Clustered Servers

In order to test on a cluster of WildFly servers, the default configuration serves pretty well, you just need to change startup script a bit - in following scenario we will use servers colocated on one node with configured port-offset:

    ./bin/standalone.sh -c standalone-full-ha.xml -Djboss.node.name=node1 -Djboss.messaging.cluster.password=somepassword -Djboss.socket.binding.port-offset=100 -Djava.net.preferIPv4Stack=true

And in a second terminal:

    ./bin/standalone.sh -c standalone-full-ha.xml -Djboss.node.name=node2 -Djboss.messaging.cluster.password=somepassword -Djboss.socket.binding.port-offset=200 -Djava.net.preferIPv4Stack=true

Note: on OS X, you need to enable multicast first:

    # Adds a multicast route for 224.0.0.1-231.255.255.254
    sudo route add -net 224.0.0.0/5 127.0.0.1

#### Getting Started with Clustered Servers

In order to test on a cluster of WildFly servers, the default configuration serves pretty well, you just need to change startup script a bit - in following scenario we will use servers colocated on one node with configured port-offset:

    ./bin/standalone.sh -c standalone-full-ha.xml -Djboss.node.name=node1 -Djboss.messaging.cluster.password=somepassword -Djboss.socket.binding.port-offset=100 -Djava.net.preferIPv4Stack=true

And in a second terminal

    ./bin/standalone.sh -c standalone-full-ha.xml -Djboss.node.name=node2 -Djboss.messaging.cluster.password=somepassword -Djboss.socket.binding.port-offset=200 -Djava.net.preferIPv4Stack=true

Note: on OS X, you need to enable multicast first:

    # Adds a multicast route for 224.0.0.1-231.255.255.254
    sudo route add -net 224.0.0.0/5 127.0.0.1
    
## Docker-Compose

For your convenience, we do have an easy way of launch with our [Docker compose file](docker-compose)

## Documentation

For more details about the current release, please consult [our documentation] (https://github.com/aerobase/omnibus-unifiedpush-server/tree/master/doc) or visit [AeroGear documentation] 
(https://aerogear.org/getstarted/guides/#push).

#### Generate REST Documentation

Up to date generated REST endpoint documentation can be found in `jaxrs/target/miredot/index.html`. It is generated with every `jaxrs` module build.

## Who is using it?

We have a list of users in our [wiki](https://github.com/aerobase/unifiedpush-server/wiki/Users-of-the-UnifiedPush-Server). If you are using the UnifiedPush Server, please add yourself to the list!

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

For instructions how to develop `admin-ui`, refer to [`admin-ui/README.md`](https://github.com/aerobase/unifiedpush-server/blob/master/admin-ui/README.md).

These instructions contains also specific instructions how to upgrade NPM package dependencies.

Note that the {{frontend-maven-plugin}} may fail if you killed the build during its work - it may leave the downloaded modules in inconsistent state, see [`admin-ui/README.md`](https://github.com/aerobase/unifiedpush-server/blob/master/admin-ui/README.md#build-errors).

#### Cleaning the Admin UI build

In order to clean the state of Admin UI build caches, run maven build with the following parameter

    mvn clean install -Dfrontend.clean.force

Try this if the build fails e.g. after `bower.json` or `package.json` modifications to make sure no cache is playing with you.


=======
## Releasing the AeroBase UnifiedPush Server

The content of the [Release Process](https://github.com/aerogear/collateral/wiki/Release-Process-(Java)) is valid for this project as well. However, to build the `distribution` bundle, you need to include these profiles:

```
mvn release:GOAL -Pdist,test
```

## Deprecation Notices

###  1.1.x

*Chrome Packaged Apps*

The Chrome Packaged App Variant will be removed.  Google has deprecated the [chrome.pushMessaging API](https://developer.chrome.com/extensions/pushMessaging) in favor of the [chrome.gcm API](https://developer.chrome.com/extensions/gcm).

This change allows the UnifiedPush Server to now use the Android Variant for both Android and Chrome Apps.

If you are using this functionality, please convert your applications to use the new API and recreate your variants.


## How to develop and run tests

There is a dedicated guide to running and developing tests in [TESTS.md](./TESTS.md)

## Found a bug?

If you found a bug please create a ticket for us on [Issues](https://github.com/aerobase/unifiedpush-server/issues) with some steps to reproduce it.
