# AeroGear UnifiedPush Server

[![Build Status](https://travis-ci.org/aerogear/aerogear-unifiedpush-server.png)](https://travis-ci.org/aerogear/aerogear-unifiedpush-server)
[![License](https://img.shields.io/:license-Apache2-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.jboss.aerogear.unifiedpush/unifiedpush-parent/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.jboss.aerogear.unifiedpush/unifiedpush-parent)

The _AeroGear UnifiedPush Server_ is a server that allows sending push notifications to different (mobile) platforms and has support for:
* [Apple’s APNs (HTTP/2)](https://developer.apple.com/library/content/documentation/NetworkingInternet/Conceptual/RemoteNotificationsPG/APNSOverview.html#//apple_ref/doc/uid/TP40008194-CH8-SW1)
* [Firebase Cloud Messaging](https://firebase.google.com/docs/cloud-messaging/)
* [Microsoft's Windows Push Notification service (WNS)](https://msdn.microsoft.com/en-us/library/windows/apps/hh913756.aspx)

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

The easiest way to get started is running our `plain` Linux container:
```
mvn clean install -DskipTests && docker run -p 18081:8080 -it aerogear/ups:plain 
```

then go to `http://localhost:18081/` to use the UPS.

For our on-premise version, and more infos on database setup, please consult our [user guide](https://aerogear.org/docs/unifiedpush/ups_userguide/index)!

### Docker-Compose

For your convenience, we do have an easy way of launch different configurations of the UPS, using our [Docker compose files](docker-compose)

## Documentation

For more details about the current release, please consult [our documentation](https://aerogear.org/getstarted/guides/#push).

## Who is using it?

We have a list of users in our [wiki](https://github.com/aerogear/aerogear-unifiedpush-server/wiki/Users-of-the-UnifiedPush-Server). If you are using the UnifiedPush Server, please add yourself to the list!

## Development

Build the project:
```
mvn clean install
```

and start the latest build, locally, like `docker run -p 18081:8080 -it aerogear/ups:plain`

## Configuration

The Unified Push Server can be configured with either System Properties (passed to the Java commandline) or Environment Variables. The two options have different formats and the following list describes them using `System Property Name`/`Env Var Name`: `Purpose`.

* _custom.aerogear.apns.push.host/CUSTOM_AEROGEAR_APNS_PUSH_HOST_: Custom host for sending Apple push notifications. Can be used for testing
* _custom.aerogear.apns.push.port/CUSTOM_AEROGEAR_APNS_PUSH_PORT_: Custom port for the Apple Push Network host
* _custom.aerogear.fcm.push.host/CUSTOM_AEROGEAR_FCM_PUSH_HOST_: Custom host for sending Google Firebase push notifications. Can be used for testing
* _ups.realm.name/UPS_REALM_NAME_: Override Keycloak Realm
* _ups.auth.server.url/UPS_AUTH_SERVER_URL_: Override Keycloak authentication redirect
* _aerogear.metrics.storage.days/AEROGEAR_METRICS_STORAGE_DAYS_: Override the number of days the metrics are stored (default is 30 days)
* _ups.amqp.server.url/ARTEMIS_URL_ : URL For AMQP Server
* _ups.amqp.server.port/ARTEMIS_PORT_ : PORT For AMQP Server
* _ups.amqp.server.username/ARTEMIS_PASSWORD_: Password for AMQP server
* _ups.amqp.server.password/ARTEMIS_USERNAME_: Username for AMQP server


## Releasing the UnifiedPush Server

The content of the [Release Process](https://github.com/aerogear/collateral/wiki/Release-Process-(Java)) is valid for this project as well. However, to build the full `distribution` bundle, you need to fire off the release like:

```
## prepare the release and define the TAG and adjust the versions:
mvn release:prepare -Dtag=x.y.z.Final -Darguments=-Dgpg.passphrase=$MY_SECRET_PASS_PHRASE -Pdist,test

## run the actual release process and load the artifacts to JBoss Nexus
mvn release:perform -DperformRelease=true -Darguments=-Dgpg.passphrase=$MY_SECRET_PASS_PHRASE -Dgpg.useagent=true -Pdist,test
```

## Contributing

If you would like to help develop AeroGear you can join our [developer's mailing list](https://lists.jboss.org/mailman/listinfo/aerogear-dev), join #aerogear on Freenode, or shout at us on Twitter @aerogears.

Also takes some time and skim the [contributor guide](http://aerogear.org/docs/guides/Contributing/)

We are available on [Sonarcloud.io](https://sonarcloud.io/dashboard?id=org.jboss.aerogear.unifiedpush%3Aunifiedpush-parent) if you want to help us reduce our technical debt or improve our test coverage check us out there. It's a great way to get involved with your first PR. Check out the excellent guide on running [SonarQube](https://docs.sonarqube.org/display/SCAN/Analyzing+with+SonarQube+Scanner+for+Maven) locally for more information.

## Questions?

Join our [user mailing list](https://lists.jboss.org/mailman/listinfo/aerogear-users) for any questions or help! We really hope you enjoy app development with AeroGear!

## Found a bug?

If you found a bug please create a ticket for us on [Jira](https://issues.jboss.org/browse/AGPUSH) with some steps to reproduce it.
