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

Unified Push uses Hibernate as an ORM layer.  This means that as long as there is a Hibernate dialect for you database you can reconfigure Unified Push to work with your database.  The shipped container image supports postgres.  When using the default container, you may use the environment variables `POSTGRES_SERVICE_PORT`, `POSTGRES_SERVICE_HOST`, `POSTGRES_USER`, and `POSTGRES_PASSWORD`.  The postgres user will need to be able to create tables inside of its table space.

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
In production security and scalability are very important concerns.  Unified Push supports using Keycloak to provide user athentication, and it is horizontally scalable if you provide an external AMQ broker like Artemis.

### Using Keycloak for Authentication
[Keycloak](https://www.keycloak.org/) is an authentication service that provides out of the box OAuth support for single sign on.  By setting the correct environment variables, Unified Push will require users to log into Keycloak before they are allowed access to the Unified Push console. Setting `KEYCLOAK_SERVICE_HOST` and `KEYCLOAK_SERVICE_PORT` will trigger Unified Push to look for a Keycloak service running at `KEYCLOAK_SERVICE_HOST:KEYCLOAK_SERVICE_PORT`.  Additionally you will need to configure a keycloak realm named *aerogear* with a client *unifies-push-server* that provides a user with a role named 'admin', or use our [sample realm](https://github.com/aerogear/aerogear-unifiedpush-server/blob/master/docker-compose/keycloak-realm/ups-realm-sample.json).

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

### Using an external AMQ broker 


- AeroGear 10146
## Running with Operator
- AeroGear 10145

## Configuration
 - AEROGEAR-10148