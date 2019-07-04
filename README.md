# AeroGear UnifiedPush Server

[![Build Status](https://travis-ci.org/aerogear/aerogear-unifiedpush-server.png)](https://travis-ci.org/aerogear/aerogear-unifiedpush-server)
[![License](https://img.shields.io/:license-Apache2-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.jboss.aerogear.unifiedpush/unifiedpush-parent/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.jboss.aerogear.unifiedpush/unifiedpush-parent)

The _AeroGear UnifiedPush Server_ is a server that allows sending push notifications to different (mobile) platforms and has support for:
* [Apple’s APNs (HTTP/2)](https://developer.apple.com/library/content/documentation/NetworkingInternet/Conceptual/RemoteNotificationsPG/APNSOverview.html#//apple_ref/doc/uid/TP40008194-CH8-SW1)
* [Firebase Cloud Messaging](https://firebase.google.com/docs/cloud-messaging/)

<img src="https://raw.githubusercontent.com/aerogear/aerogear-unifiedpush-server/master/ups-ui-screenshot.png" height="427px" width="550px" />

## Project Info

|                 | Project Info                                                     |
| --------------- | ---------------------------------------------------------------- |
| License:        | Apache License, Version 2.0                                      |
| Build:          | Maven                                                            |
| Documentation:  | https://docs.aerogear.org                                        |
| Issue tracker:  | https://issues.jboss.org/browse/AEROGEAR                         |
| Mailing lists:  | [aerogear-dev](https://groups.google.com/forum/#!forum/aerogear) |

## Getting started

The easiest way to get started is running our `unifiedpush-configurable-container` Linux container:

```
mvn clean install -DskipTests
docker run -p 9999:8080 -it aerogear/unifiedpush-configurable-container:2.2.2-SNAPSHOT 
```

then go to `http://localhost:9999/` to use the UPS.

### Docker-Compose

For your convenience, we do have an easy way of launch different configurations of the UPS, using our [Docker compose files](docker-compose)

## Container Configuration

The Unified Push Server build by default creates and registers a Docker formatted container image, aerogear/unifiedpush-configurable-container.  By default this container uses an in-memory database, in-vm amq messaging, and no authentication.  While ideal for testing, for actual use the following environment variables should be set 

* _KEYCLOAK_SERVICE_HOST_: URL of a KeyCloak server providing authentication.
* _KEYCLOAK_SERVICE_PORT_: KeyCloak service port.

* _POSTGRES_SERVICE_HOST_: URL of Postgres database
* _POSTGRES_SERVICE_PORT_: Port to connect to Postgres database
* _POSTGRES_USER_: Postgres username to use
* _POSTGRES_PASSWORD_: Postgres password to use
* _POSTGRES_DATABASE_: Postgres database for UPS

* _ARTEMIS_SERVICE_HOST_: Artemis AMQ service URL
* _ARTEMIS_SERVICE_PORT_: Artemis AMQ service Port
* _ARTEMIS_USER_: Artemis AMQ service username
* _ARTEMIS_PASSWORD_: Artemis AMQ service password

## Configuration

The Unified Push Server can be configured with either System Properties (passed to the Java commandline) or Environment Variables. The two options have different formats and the following list describes them using `System Property Name`/`Env Var Name`: `Purpose`.

* _CUSTOM_AEROGEAR_APNS_PUSH_HOST_: Custom host for sending Apple push notifications. Can be used for testing
* _CUSTOM_AEROGEAR_APNS_PUSH_PORT_: Custom port for the Apple Push Network host
* _CUSTOM_AEROGEAR_FCM_PUSH_HOST_: Custom host for sending Google Firebase push notifications. Can be used for testing
* _UPS_REALM_NAME_: Override Keycloak Realm
* _KEYCLOAK_SERVICE_HOST_: Override Keycloak authentication redirect
* _AEROGEAR_METRICS_STORAGE_DAYS_: Override the number of days the metrics are stored (default is 30 days)
* _ARTEMIS_URL_ : URL For AMQP Server
* _ARTEMIS_PORT_ : PORT For AMQP Server
* _ARTEMIS_PASSWORD_: Password for AMQP server
* _ARTEMIS_USERNAME_: Username for AMQP server

## Releasing the UnifiedPush Server

The content of the [Release Process](https://github.com/aerogear/collateral/wiki/Release-Process-(Java)) is valid for this project as well. However, to build the full `distribution` bundle, you need to fire off the release like:

```
## prepare the release and define the TAG and adjust the versions:
mvn release:prepare -Dtag=x.y.z.Final -Darguments=-Dgpg.passphrase=$MY_SECRET_PASS_PHRASE -Pdist,test

## run the actual release process and load the artifacts to JBoss Nexus
mvn release:perform -DperformRelease=true -Darguments=-Dgpg.passphrase=$MY_SECRET_PASS_PHRASE -Dgpg.useagent=true -Pdist,test
```

## License 

See [LICENSE file](./LICENSE.txt)

## Contributing

[General Contributing Guide](.github/CONTRIBUTING.md)

## Questions?

Join our [user mailing list](https://groups.google.com/forum/#!forum/aerogear) for any questions or help! We really hope you enjoy app development with AeroGear!

## Found a bug?

If you found a bug please create a ticket for us on [Jira](https://issues.jboss.org/browse/AEROGEAR) with some steps to reproduce it.
