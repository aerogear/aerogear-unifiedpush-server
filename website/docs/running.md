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
podman run -p 9999:8080 -it quay.io/aerogear/unifiedpush-configurable-container:master
```

```podman
docker run -p 9999:8080 -it quay.io/aerogear/unifiedpush-configurable-container:master
```

You should now be able to access the Unified Push admin ui at `http://localhost:9999`. This is great for quick tests, demonstrations, etc.  If you would like to keep data between launches of Unified Push, please use the other guides in this document.

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

The UnifiedPush Server can be installed and run on OpenShift by using the UnifiedPush Operator. The UnifiedPush Server is created by the Operator from Custom Resource yaml files defined as per your specifications in the operator deploy directory.  

Prerequisite installations required for _UnifiedPush Operator_

[Install Go.](https://golang.org/doc/install)

[Set your $GOPATH evironment variable.](https://github.com/golang/go/wiki/SettingGOPATH)

[Install the dep package manager.](https://golang.github.io/dep/docs/installation.html)

[Install Operator SDK.](https://github.com/operator-framework/operator-sdk#quick-start)

[Install kubectl.](https://kubernetes.io/docs/tasks/tools/install-kubectl/#install-kubectl)

:::note 
Currently _UnifiedPush Operator_ is only supported by v0.10.1 of the Operator SDK
:::

### Configuration

The UnifiedPush Server Image and The PostgreSQL Image that get used by the UPS Operator are recommended not to be changed. We support the released versions of the UPS Operator to manage the versions of the UnifiedPush Server and PostgreSql severs released with it. 

:::note 
The Images used can be found in the [constants.go file](https://github.com/aerogear/unifiedpush-operator/blob/master/pkg/constants/constants.go)
:::
The UnifiedPush Server does have configurable fields that can be defined in the the custom resource yaml file used in the `make install` command of the UnifiedPush Operator Makefile. Here are the configurable fields in a _UnifiedPush Server_:

| Field Name  | Description  |  Default |
|---|---|---|
| Backups  |<pre>A list of backup entries that CronJobs will be created from [here](https://github.com/aerogear/unifiedpush-operator/blob/master/deploy/crds/push_v1alpha1_unifiedpushserver_cr_with_backup.yaml) for an annotated example.<br>Note that a ServiceAccount called "backupjob" must already exist before the operator will create any back CronJobs.<br>See the [backup-container-image](https://github.com/integr8ly/backup-container-image/tree/master/templates/openshift/rbac) for examples. |  No Backups |
| useMessageBroker  | Can be set to true to use managed queues, if you are using enmasse.| false |
|  unifiedPushResourceRequirements | Unified Push Service container resource requirements.| <Pre> limits: <br> memory: "value of UPS_MEMORY_LIMIT passed to operator"<br> cpu: "value of UPS_CPU_LIMIT passed to operator"<br>requests:<br> memory: "value of UPS_MEMORY_REQUEST passed to operator" <br> cpu: "value of UPS_CPU_REQUEST passed to operator"|
| oAuthResourceRequirements  | OAuth Proxy container resource requirements.  | limits: <br> memory: "value of OAUTH_MEMORY_LIMIT passed to operator" <br> cpu: "value of OAUTH_CPU_LIMIT passed to operator"<br> requests: <br> memory: "value of OAUTH_MEMORY_REQUEST passed to operator" <br> cpu: "value of OAUTH_CPU_REQUEST passed to operator"|
| postgresResourceRequirements  | Postgres container resource requirements.  | limits: <br> memory: "value of POSTGRES_MEMORY_LIMIT passed to operator" <br> cpu: "value of POSTGRES_CPU_LIMIT passed to operator"<br> requests: <br> memory: "value of POSTGRES_MEMORY_REQUEST passed to operator" <br> cpu: "value of POSTGRES_CPU_REQUEST passed to operator"|
| postgresPVCSize  | PVC size for Postgres service  | Value of POSTGRES_PVC_SIZE environment variable passed to operator |

If the values for these fields are not defined by you, in the UnifiedPush Server CR, the UnifiedPush Operator will use some default values that are passed to the operator as environment variables. If no environment variable is also passed to the operator, it will use some hardcoded values. The default values for resource sizes, limits and requests can be seen in this table

| Variable | Default Value |
|----------|---------------|
|UPS_MEMORY_LIMIT | 2Gi |
|UPS_MEMORY_REQUEST | 512Mi | 
|UPS_CPU_LIMIT | 1 | 
|UPS_CPU_REQUEST | 500m |
|OAUTH_MEMORY_LIMIT | 64Mi |
|OAUTH_MEMORY_REQUEST | 32Mi |
|OAUTH_CPU_LIMIT | 20m |
|OAUTH_CPU_REQUEST | 10m |
|POSTGRES_MEMORY_LIMIT | 512Mi |
|POSTGRES_MEMORY_REQUEST | 256Mi |
|POSTGRES_CPU_LIMIT | 1 |
|POSTGRES_CPU_REQUEST | 250m |
|POSTGRES_PVC_SIZE | 5Gi |

The [crds directory](https://github.com/aerogear/unifiedpush-operator/tree/master/deploy/crds) in the UnifiedPush Operator contains yaml files that can be used as examples of how to configure the different UnifiedPush fields. To implement these different yaml files and test them for yourself simply change the name of the custom Resource yaml file at line 76 of the Makefile. 
 ```bash
 - kubectl apply -n $(NAMESPACE) -f deploy/crds/push_v1alpha1_unifiedpushserver_cr.yaml
 ```
to 

```bash
 - kubectl apply -n $(NAMESPACE) -f deploy/crds/push_v1alpha1_unifiedpushserver_cr_with_backup.yaml
```

The container names deployed by the UnifiedPush Operator can also be modified, the following table shows the environment variables and the default names for the containers.


| Name | Default |
|------|---------|
| UPS_CONTAINER_NAME | ups |
| OAUTH_PROXY_CONTAINER_NAME | ups-oauth-proxy |
| POSTGRES_CONTAINER_NAME | postgresql |

### Development

_UnifiedPush_ can be easily installed and maintained on OpenShift by using the _UnifiedPush Operator_. This Operator makes managing the UnifiedPush Server and Database a seamless process.There are a number of prerequisites to getting started with the UnifiedPush Operator.



####  Operator Installation and Usage

The UnifiedPush Operator needs to be downloaded and installed in the github.com directory inside the go directory. You can clone it and add it to you $GOPATH with the following command

```bash
git clone git@github.com:aerogear/unifiedpush-operator.git $GOPATH/src/github.com/aerogear/unifiedpush-operator
```

Once this is finished you can open a terminal in this directory and install the operator by using: 

```bash
 make install
 ```

This command is defined in the Operators Makefile.

:::note
To install the operator you must be logged in as a user with cluster privileges
:::

With the Operator installed, switch to using that project with the `oc project unifiedpush` command. Prepare the cluster and install all prerequisites for the UnifiedPush Operator 

```bash
make cluster/prepare
```

Once this is finished you can then start the _UnifiedPush Operator_ locally with the command:

```bash
make code/run
```
In another terminal you can use the `oc get route` command to get the host address for the _UnifiedPush Server_ admin console on Openshift. You will need to login using your Openshift  username and password.

If you are finished using the Operator and want to remove it from your cluster and clean up, you can use the command:

```bash
make cluster/clean
```
:::note
The Makefile commands for the _UnifiedPush Operator_ can be further explored [here](https://github.com/aerogear/unifiedpush-operator/blob/master/Makefile)
:::

### Monitoring UnifiedPush Operator & Server

Monitoring of the UnifiedPush Operator and UnifiedPush Server can be done using the  [integr8ly/Grafana](https://github.com/coreos/prometheus-operator) and [Prometheus](https://github.com/coreos/prometheus-operator) Operator. These can be installed through OperatorHub onto your

The UnifiedPush Operator will install it’s own monitoring resources required by Grafana and Prometheus on startup and will install the Resources required for monitoring the UnifiedPush Server on creation of the UnifiedPushServer CR. 

:::note
These will be ignored if the required CRDs are not installed on the cluster. Restart the operator to install the resources if the application-monitoring stack is deployed afterwards.
:::

### Further Reading

Further information documenting testing the Operator, publishing images, tagging releases and the operator architecture can be found on the [UnifiedPush Operator Readme on Github](https://github.com/aerogear/unifiedpush-operator/blob/master/README.adoc)
