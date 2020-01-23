---
id: running
title: Running Unified Push
sidebar_label: Running Unified Push
---

Before Unified Push can send push messages, it must be running. This page has guides for a quick; no dependency startup, a stable, low dependency setup, and a full k8s operator managed setup.

## Prerequisites
 * You will need a container management system installed.  On Linux and Mac this includes [podman](https://podman.io), and [Docker](https://docker.io) is available on most operating systems.

## Quick Start  

Unified Push is released as a Docker formatted container on [quay.io](https://quay.io/repository/aerogear/unifiedpush-configurable-container).  By default it will launch a self contained, in memory instance of Unified Push. 

All you need is a container manager tool like podman or the Docker CLI.

```podman
podman run -p 9999:8080 -it aerogear/unifiedpush-configurable-container:latest
```

```podman
docker run -p 9999:8080 -it aerogear/unifiedpush-configurable-container:latest
```

You should now be able to access the Unified Push admin ui at http://localhost:9999. This is great for quick tests, demonstrations, etc.  If you would like to keep data between launches of Unified Push, please use the other guides in this document.

## Running for app developers
If you are an app developer and wanting to run Unified Push locally on your development machine, you probably want to have your configuration persist between environment restarts. Additionally, you may need to configure SSL certificates for native push on some devices and operating systems.

### Enabling persistence with Postgres

Unified Push uses Hibernate as an ORM layer, and the shipped container image supports postgres.  To setup its table space, Unified Push needs to be given a postgres user that can create tables.

The container supports the following environment variables to configure connecting to a postgres database

Name|Description|
----|-----------|
POSTGRES_USER|A username to connect to Postgres|
POSTGRES_PASSWORD|A password to connect Postgres|
POSTGRES_SERVICE_HOST|Postgres server hostname or ip address|
POSTGRES_SERVICE_PORT|Postgres server port|

For example, if you had the following postgres database : 
POSTGRES_SERVICE_HOST|POSTGRES_SERVICE_PORT|POSTGRES_USER|POSTGRES_PASSWORD
---------------------|---------------------|-------------|-----------------
172.17.0.2           |5432                 | unifiedpush |unifiedpush

You would run Unified Push with the following podman command

```bash
podman run -p 8080:8080 --rm \
  -e POSTGRES_USER=unifiedpush \
  -e POSTGRES_PASSWORD=unifiedpush \
  -e POSTGRES_SERVICE_HOST=172.17.0.2 \
  -e POSTGRES_SERVICE_PORT=5432 \
  -e POSTGRES_DATABASE=unifiedpush \
  quay.io/aerogear/unifiedpush-configurable-container:master
```

### Using SSL Certificates


## Running in Production
In production security and scalability are very important concerns.  Unified Push supports using Keycloak to provide user athentication, and it is horizontally scalable if you provide an external AMQP broker like Artemis.

### Using Keycloak for Authentication
[Keycloak](https://www.keycloak.org/) is an authentication service that provides out of the box OAuth support for single sign on.  By setting the correct environment variables, Unified Push will require users to log into Keycloak before they are allowed access to the Unified Push console. Additionally you will need to configure a keycloak realm using our [sample realm](https://github.com/aerogear/aerogear-unifiedpush-server/blob/master/docker-compose/keycloak-realm/ups-realm-sample.json).


The container supports the following environment variables to configure keycloak integration

Name|Description|
----|-----------|
KEYCLOAK_SERVICE_HOST|Keycloak server hostname or ip address|
KEYCLOAK_SERVICE_PORT|Keycloak server port|


A keycloak with the following configuration

KEYCLOAK_SERVICE_HOST|KEYCLOAK_SERVICE_PORT|
---------------------|---------------------|
172.17.0.2           |8080                 |

would be run with the following podman command 

```bash
podman run -p 8080:8080 --rm \
  -e KEYCLOAK_SERVICE_HOST=172.17.0.2 \
  -e KEYCLOAK_SERVICE_PORT=8080 \
  quay.io/aerogear/unifiedpush-configurable-container:master
```

### Using an external AMQP broker 
Unified Push uses JMS to schedule communication with native push services such as Firebase or APNS. Unified Push by default runs its own JMS broker, but it can use an external message broker with the AMQP specification such as Enmasse or Apache Artemis. Using an external broker lets you spread out the workload of sending messages among several Unified Push instances.  If the user is allowed, Unified Push will create the messaging resources it needs, otherwise this should be done before hand.

The Unified Push container uses the following variables to define and enable an external AMQP broker connection.

Name|Description|
----|-----------|
ARTEMIS_USER|A username to connect to an AMQP server|
ARTEMIS_PASSWORD|A password to connect to an AMQP server|
ARTEMIS_SERVICE_HOST|AMQP server hostname or ip address|
ARTEMIS_SERVICE_PORT|AMQP server port|
AMQ_MAX_RETRIES|'optional' Number of times to retry sending a push message before discarding the JMS message. <br>*Default 3*|
AMQ_BACKOFF_SECONDS|'optional' Number of seconds to delay retrying a JMS message. <br>*Default 10*|

If you wished to connect to the following Artemis acceptor :

ARTEMIS_SERVICE_HOST  |ARTEMIS_SERVICE_PORT|ARTEMIS_USER|ARTEMIS_PASSWORD|
---------------------|---------------------|------------|----------------|
172.17.0.9           |61616                 |messageuser|messagepassword|

you would run the following podman command 

```bash
podman run -p 8080:8080 --rm \
  -e ARTEMIS_SERVICE_HOST=172.17.0.9 \
  -e ARTEMIS_SERVICE_PORT=61616 \
  -e ARTEMIS_USER=messageuser \
  -e ARTEMIS_PASSWORD=messagepassword \
  quay.io/aerogear/unifiedpush-configurable-container:master
```

## Running with Operator
- AeroGear 10145

## Configuration
 - AEROGEAR-10148