---
id: maintenance
title: Maintaining Unified Push
sidebar_label: Maintaining Unified Push
---

## Backup
 - AEROGEAR-10132	
## Upgrade

### Upgrading to Container Service
You can upgrade from UPS from being a containerless service to a containerized service. Unified Push is released as a Docker formatted container available on quay.io. This container uses a in-memory database, in-vm amq messaging and no authentication by default. The following environment variables are required to be set for actual use. 

To run Unified Push as a container you only need a container manager tool such as podman or the Docker CLI.

`podman run -p 9999:8080 -it aerogear/unifiedpush-configurable-container:latest`

`docker run -p 9999:8080 -it aerogear/unifiedpush-configurable-container:latest`

For authentication to access the Unified Push console the following KeyCloak environment variables must be set.

Name|Description|
----|-----------|
`KEYCLOAK_SERVICE_HOST`|URL of a KeyCloak server providing authentication| 
`KEYCLOACK_SERVICE_PORT`|KeyCloak service port|

this would be done with the following podman command

```bash
 podman run -p 8080:8080 --rm \
   -e KEYCLOAK_SERVICE_HOST=172.17.0.2 \
   -e KEYCLOAK_SERVICE_PORT=8080 \
   quay.io/aerogear/unifiedpush-configurable-container:master
```

To enable data persistance these are the environment variables for connecting a UPS container to a database you have already created.

:::note 
UPS supports PostgreSQL and MySQL, the following environment variables are done for a PostgreSQL database
:::

Name|Description|
----|-----------|
`POSTGRES_SERVICE_PORT`|Port to connect to Postgres database.|
`POSTGRES_SERVICE_HOST`|URL of Postgres database.|
`POSTGRES_USER`|Postgres username to use.|
`POSTGRES_PASSWORD`|Postgres password to use.|

you would run the following command to connect to a PostgreSQL database

```bash
 podman run -p 8080:8080 --rm \
   -e POSTGRES_USER=unifiedpush \
   -e POSTGRES_PASSWORD=unifiedpush \
   -e POSTGRES_SERVICE_HOST=172.17.0.2 \
   -e POSTGRES_SERVICE_PORT=5432 \
   -e POSTGRES_DATABASE=unifiedpush \
   quay.io/aerogear/unifiedpush-configurable-container:master
```

Unified Push uses JMS to schedule communication by default, but can be configured to use an external message broker with the AMQP specification such as Enmasse or Apache Artemis. The following environment variables are used for an external AMQP broker connection with Apache Artemis.

Name|Description|
----|-----------|
`ARTEMIS_SERVICE_HOST`| Artemis AMQ service URL.|
`ARTEMIS_SERVICE_PORT`| Artemis AMQ service Port.|
`ARTEMIS_USER`|Artemis AMQ service username.|
`ARTEMIS_PASSWORD`|Artemis AMQ service password.|

these would be set by running the following podman command

```bash
 podman run -p 8080:8080 --rm \
   -e ARTEMIS_SERVICE_HOST=172.17.0.9 \
   -e ARTEMIS_SERVICE_PORT=61616 \
   -e ARTEMIS_USER=messageuser \
   -e ARTEMIS_PASSWORD=messagepassword \
   quay.io/aerogear/unifiedpush-configurable-container:master
```
