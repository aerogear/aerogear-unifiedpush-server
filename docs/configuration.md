# Configuration

// TODO Should we explain how to run the UPS as a war?

## Container Configuration

The Unified Push Server build by default creates and registers a Docker formatted container image, 
aerogear/unifiedpush-configurable-container.  By default this container uses an in-memory database, 
in-vm amq messaging, and no authentication.  While ideal for testing, for actual use the following 
environment variables should be set

| **Environment Variable** | **Description**                                    |
|--------------------------|----------------------------------------------------|
| `KEYCLOAK_SERVICE_HOST`  | URL of a KeyCloak server providing authentication. |
| `KEYCLOAK_SERVICE_PORT`  | KeyCloak service port.                             |
| `POSTGRES_SERVICE_PORT`  | Port to connect to Postgres database.              |
| `POSTGRES_SERVICE_HOST`  | URL of Postgres database.                          |
| `POSTGRES_USER`          | Postgres username to use.                          |
| `POSTGRES_PASSWORD`      | Postgres password to use.                          |
| `ARTEMIS_SERVICE_HOST`   | Artemis AMQ service URL.                           |
| `ARTEMIS_SERVICE_PORT`   | Artemis AMQ service Port.                          |
| `ARTEMIS_USER`           | Artemis AMQ service username.                      |
| `ARTEMIS_PASSWORD`       | Artemis AMQ service password.                      |


##  Unified Push Server

The Unified Push Server can be configured with either System Properties (passed to the Java commandline) 
or Environment Variables. The two options have different formats and the following list describes them 
using `System Property Name`/`Env Var Name`: `Purpose`.

| **Environment Variable**         | **Description**                                                                      |
|----------------------------------|--------------------------------------------------------------------------------------|
| `CUSTOM_AEROGEAR_APNS_PUSH_HOST` | Custom host for sending Apple push notifications.                                    |
| `CUSTOM_AEROGEAR_APNS_PUSH_PORT` | Custom port for the Apple Push Network host.                                         |
| `CUSTOM_AEROGEAR_FCM_PUSH_HOST`  | Custom host for sending Firebase push notifications.                                 |
| `UPS_REALM_NAME`                 | Override Keycloak Realm.                                                             |
| `KEYCLOAK_SERVICE_HOST`          | Override Keycloak authentication redirect.                                           |
| `AEROGEAR_METRICS_STORAGE_DAYS`  | Override the number of days the metrics are stored (default is 30 days).             |
| `ARTEMIS_URL`                    | URL For AMQP Server.                                                                 |
| `ARTEMIS_PORT`                   | PORT For AMQP Server.                                                                |
| `ARTEMIS_PASSWORD`               | Password for AMQP server.                                                            |
| `ARTEMIS_USERNAME`               | Username for AMQP server.                                                            |
