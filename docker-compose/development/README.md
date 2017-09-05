# Keycloak and MySQL - Development Environment

For running UPS, you need a database and a running Keycloak broker. This folder contains a _development_ environment, that spins up a MySQL 5.x DB and a Keycloak server that imports our demo realm.

## Getting started

To run these servers, simply execute the following command:

```
docker-compose -f development-env.yaml up
```

## Running the UPS

To run the UPS `WAR` file, you need to know the IP address of the keycloak broker, you can extract it like:

```
docker inspect --format '{{ .NetworkSettings.IPAddress }}' name_or_id_of_the_keycloak_container
```

That value needs to be used when launching the _prepared_ WildFly11 container, like:

```
/$WF_HOME/bin/standalone.sh -Dups.realm.name=aerogear -Dups.auth.server.url=http://172.17.0.7:8080/auth -b 0.0.0.0 

```

**NOTE:** This is for development only - in production you should not use wildcard URLs!


