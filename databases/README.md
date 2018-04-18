# Install the UPS with a RDBMS

This guide explains how to configure a fresh WildFly instance and setup the database

__Perquisite:__ Make sure you have the latest version of [WildFly](http://wildfly.org/downloads/), extracted on your machine.

## Database configuration

The UPS does support two database system:

* Postgres (preferred)
* MySQL

### Postgres

You need a running database, like:

```
docker run \
           -p 5432:5432 \
           -e POSTGRES_PASSWORD=unifiedpush \
           -e POSTGRES_USER=unifiedpush \
           -e POSTGRES_DATABASE=unifiedpush \
           -d postgres:9.6
```

### MySQL 

You need a running database, like:

```
docker run \
           -p 6306:3306 \
           -e MYSQL_USER=unifiedpush \
           -e MYSQL_PASSWORD=unifiedpush \
           -e MYSQL_DATABASE=unifiedpush \
           -e MYSQL_ROOT_PASSWORD=supersecret \
           -d mysql:5.6
```

## Prepare the Wildfly server for UPS deployment

Go to your Wildfly installation and start the server, using the `standalone-full` configuration, like:

```
$WILDFLY_HOME/bin/standalone.sh -c standalone-full.xml -b 0.0.0.0
```

__NOTE:__ make sure you have the installation folder of Wildfly exported to `$WILDFLY_HOME`.

We have two shell scripts for setting up the system:

* `prepare_clean_psql_wildfly.sh`
* `prepare_clean_mysql_wildfly.sh`

Based on your database choice execute one of the scripts, like:

```
./prepare_clean_psql_wildfly.sh
```

## UPS deployment

Now the server is running and you can deploy the `WAR` file from the `servers/plain/target` folder.
This is an unprotected version of the push server.


## UPS and Keycloak

We have a Keycloak protected version of the UPS (located in `servers/keycloak/target`), which requires a separated Keycloak server.

For Keycloak we need to provide two `System Properties`:
* `ups.realm.name` - name of the realm used for UPS
* `ups.auth.server.url` - fullqualified URI of the Keycloak server

Here is how the command looks like:

```
$WILDFLY_HOME/bin/standalone.sh -c standalone-full.xml -Dups.realm.name=aerogear -Dups.auth.server.url=http://my-keycloak:PORT/auth -b 0.0.0.0
```

### Running Keycloak

In the [docker-compose](../docker-compose) folder there is a *Standalone Keycloak* section that explains details on how to run a Keycloak server.
