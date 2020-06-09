---
id: maintenance
title: Maintaining Unified Push
sidebar_label: Maintaining Unified Push
---

## Backup
 
 All the data required to run the _UnifiedPush Server_ is stored inside its database instance so that's all you need to backup. Currently supported databases are:
 * PostgreSQL
 * MySQL
 
 Since the _UnifiedPush Server_ runs on **WILDFLY** we can easily get information about the database by inspecting the _standalone.xml_ file:
 
<!--DOCUSAURUS_CODE_TABS-->
<!--UnifiedPush running in docker-->
If you are running _UnifiedPush Server_ from one of the provided docker images, the server is installed into the `opt` folder.

To get the content of the `standalone.xml` file, first we need to get the _docker container id_, then we must ask the content of the file
to docker:

```bash
CONTAINER_ID={YOUR CONTAINER ID}
READ_CONF="docker exec -it $CONTAINER_ID cat /opt/jboss/wildfly/standalone/configuration/standalone.xml"
bash -c "$READ_CONF"
```

The container must be up and running for this to work.  
To get the **CONTAINER_ID**, use the `docker ps` command and search for the _UnifiedPush_ container.


<!--UnifiedPush running standalone-->
 If you are running _UnifiedPush Server_ standalone in your own **WILDFLY instance**, then we just need to jump into the 
 _WILDFLY home_ to read the `standalone.xml` file:
 
```bash
WILDFLY_HOME=/path/to/your/wildfly
READ_CONF="cat $WILDFLY_HOME/standalone/configuration/standalone.xml"
bash -c "$READ_CONF"
```
<!--END_DOCUSAURUS_CODE_TABS-->
 
 :::note
 We assigned the command to the `READ_CONF` variable because it will comes handy to use it later. The command can, however, be run directly.
 :::
 
 Now that we have the configuration file, we can get all the connection details. You can manually search for a _datasource_ named
 **UnifiedPushDS** or you can use a tool like `xmlstarlet`:
 
 * **Connection URL**:
   ```bash
   CONNECTION_URL=`bash -c "$READ_CONF" | xmlstarlet sel -N x="urn:jboss:domain:datasources:5.0" -t -m "//x:datasource[@pool-name='UnifiedPushDS']//x:connection-url/text()" -c .`
   ```
 * **Username**:
   ```bash
   DB_USERNAME=`bash -c "$READ_CONF" | xmlstarlet sel -N x="urn:jboss:domain:datasources:5.0" -t -m "//x:datasource[@pool-name='UnifiedPushDS']//x:user-name/text()" -c .`
   ```
 * **Password**:
   ```bash
   DB_PASSWORD=`bash -c "$READ_CONF" | xmlstarlet sel -N x="urn:jboss:domain:datasources:5.0" -t -m "//x:datasource[@pool-name='UnifiedPushDS']//x:password/text()" -c .`
   ```
 Values are now stored into the **CONNECTION_URL**, **DB_USERNAME** and **DB_PASSWORD** environment variables:
 ```bash
 printf "CONNECTION_URL=$CONNECTION_URL \nUSERNAME=$DB_USERNAME \nPASSWORD=$DB_PASSWORD\n" 
 ```

For detailed instructions on how to backup the database, look at the official documentation"
* MySQL: https://dev.mysql.com/doc/refman/8.0/en/backup-and-recovery.html
* PostegreSQL: https://www.postgresql.org/docs/12/backup.html
 
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

### Upgrading Using UnifiedPush Operator

_UnifiedPush_ also has a _UnifiedPush_ Operator for Kubernetes. This Operator installs and manages a _UnifiedPush_ server on Openshift. To use this operator there are some prerequisites to getting started. These are outlined as follows

- You need to install [Go](https://golang.org/doc/install)
- Set the [$GOPATH environment variable](https://github.com/golang/go/wiki/SettingGOPATH)
- Install the [dep package manager](https://golang.github.io/dep/docs/installation.html)
- Install the [Operator-SDK](https://github.com/operator-framework/operator-sdk#quick-start) 
- Install the [Kubernetes command-line tool](https://kubernetes.io/docs/tasks/tools/install-kubectl/#install-kubectl) 

#### Getting Started

Once the prerequisites to getting started have been carried out you can create a local directory and clone the UnifiedPush-Operator 

`$ git clone git@github.com:aerogear/unifiedpush-operator.git $GOPATH/src/github.com/aerogear/unifiedpush-operator`

#### Minishift installation and setup

After cloning the Operator you can then install [Minishift](https://docs.okd.io/latest/minishift/getting-started/installing.html) and then install the Operators onto minishift with the following commands.

```bash
  #create a new profile to test the operator 
  $ minishift profile set unifiedpush-operator

  # enable the admin-user add-on
  $ minishift addon enable admin-user

  # add insecure registry to download the images from docker
  $ minishift config set insecure-registry 172.30.0.0/16

  # start the instance
  $ minishift start

```
:::note
  The above steps are not required in Openshift Container Platform versions greater than 4  since the Operator Lifecycle Manager and Operators come installed by default
:::

##### Installing 

Once you are logged into your Openshift cluster you can install the UnifiedPush Operator and service as follows 

`$ make install`

:::note
  To install you need to be logged in as a user with cluster privileges like the `system:admin` 
:::

##### Uninstalling

You can use the  use the following command to delete all related configurations applied to the cluster by using the following  command.

`$ make cluster/clean`

:::note 
To uninstall you need to be logged in as a user with cluster privileges like `system:admin`
:::

#### Configuration

Creation of a valid _UnifiedPush Server_ custom resource will result in a functional AeroGear _UnifiedPush Server_ deployed to your namespace

:::note 
This operator currently only supports one _UnifiedPush Server_ CR to be created
:::

This table showcases all the configurable fields in a _UnifiedPush Server_


| Field Name  | Description  |  Default |
|---|---|---|
| Backups  | A list of backup entries that CronJobs will be created from. ee [here](https://github.com/aerogear/unifiedpush-operator/blob/master/deploy/crds/push_v1alpha1_unifiedpushserver_cr_with_backup.yaml) for an annotated example. Note that a ServiceAccount called "backupjob" must already exist before the operator will create any back CronJobs. See the [backup-container-image](https://github.com/integr8ly/backup-container-image/tree/master/templates/openshift/rbac) for examples. |  No Backups |
| useMessageBroker  | Can be set to true to use managed queues, if you are using enmasse.| false |
|  unifiedPushResourceRequirements | Unified Push Service container resource requirements.| limits: <br> memory: "value of UPS_MEMORY_LIMIT passed to operator"<br> cpu: "value of UPS_CPU_LIMIT passed to operator"<br>requests:<br> memory: "value of UPS_MEMORY_REQUEST passed to operator" <br> cpu: "value of UPS_CPU_REQUEST passed to operator"|
| oAuthResourceRequirements  | OAuth Proxy container resource requirements.  | limits: <br> memory: "value of OAUTH_MEMORY_LIMIT passed to operator" <br> cpu: "value of OAUTH_CPU_LIMIT passed to operator"<br> requests: <br> memory: "value of OAUTH_MEMORY_REQUEST passed to operator" <br> cpu: "value of OAUTH_CPU_REQUEST passed to operator"|
| postgresResourceRequirements  | Postgres container resource requirements.  | limits: <br> memory: "value of POSTGRES_MEMORY_LIMIT passed to operator" <br> cpu: "value of POSTGRES_CPU_LIMIT passed to operator"<br> requests: <br> memory: "value of POSTGRES_MEMORY_REQUEST passed to operator" <br> cpu: "value of POSTGRES_CPU_REQUEST passed to operator"|
| postgresPVCSize  | PVC size for Postgres service  | Value of POSTGRES_PVC_SIZE environment variable passed to operator |

The most Basic _UnifiedPush Server_ CR doesn't specify anything in the Spec section, so the example [here](https://github.com/aerogear/unifiedpush-operator/blob/master/deploy/crds/push_v1alpha1_unifiedpushserver_cr.yaml) is a good template.

To create this, you run:

`kubectl apply -n unifiedpush -f ./deploy/crds/push_v1alpha1_unifiedpushserver_cr.yaml`

To see the created instance you can then run:

`kubectl get ups example-unifiedpushserver -n unifiedpush -o yaml`

#### Default values for CR

The previous section showed it's possible to define memory, cpu and volume limits and requests in the _UnifiedPush Server_ CR

However, the operator will use some defaults that are passed to operator as environment variables if no value is specified in the CR. If no environment variable is passed to the operator, then the operator will use some hardcoded values.

These variables and their default values can be seen here.

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

#### Container Names 

The containers come with default names, if you would like to modify them to something different, you can use the following environment variables.

| Name | Default |
|------|---------|
|UPS_CONTAINER_NAME | ups |
|OAUTH_PROXY_CONTAINER_NAME | ups-oauth-proxy |
|POSTGRES_CONTAINER_NAME | postgresql |

#### Monitoring Service (Metrics)

The application-monitoring stack provisioned by the [application-monitoring-operator](https://github.com/integr8ly/application-monitoring-operator) on [Integr8ly](https://github.com/integr8ly) can be used to gather metrics from this operator and the UnifiedPush Server. These metrics can be used by Integr8ly’s application monitoring to generate Prometheus metrics, AlertManager alerts and a dashboard in Grafana.

It is required that the [integr8ly/Grafana](https://github.com/integr8ly/grafana-operator) and [Prometheus](https://github.com/coreos/prometheus-operator) operators are installed. For further detail see [integr8ly/application-monitoring-operator](https://github.com/integr8ly/application-monitoring-operator).

The operator will install it’s own monitoring resources required by Grafana and Prometheus on startup and will install the Resources required for monitoring the UnifiedPush Server on creation of the UnifiedPushServer CR.

:::note
These will be ignored if the required CRDs are not installed on the cluster. Restart the operator to install the resources if the application-monitoring stack is deployed afterwards.
:::

### Development 

#### Running the operator

Prepare the opeator project:

`make cluster/prepare`

Run the operator locally (not in OpenShift):

`make code/run`

Create a UPS instance in another terminal:

`kubectl apply -f deploy/crds/push_v1alpha1_unifiedpushserver_cr.yaml -n unifiedpush`

Watch the status of your UPS provisioning (optional)

`watch -n1 "kubectl get po -n unifiedpush && echo '' && kubectl get ups -o yaml -n unifiedpush"`

When finished, clean up:

`make cluster/clean`

### Testing

#### Run unit tests
Unit tests are supported and can be carried out by using the command 

`make test/unit`

#### Run e2e tests

You can run end to end tests, outlined in the following steps.

Export env vars used in commands below

`export NAMESPACE="<name-of-your-openshift-project-used-for-testing>"`
`export IMAGE="quay.io/<your-account-name>/unifiedpush-operator"`

Login to OpenShift cluster as a user with cluster-admin role

`oc login <url> --token <token>`

Prepare a new OpenShift project for testing

`make NAMESPACE=$NAMESPACE cluster/prepare`

Modify the operator image name in manifest file 

`yq w -i deploy/operator.yaml spec.template.spec.containers[0].image $IMAGE`

:::note 
Note: If you do not have yq installed, just simply edit the image name in deploy/operator.yaml
:::

Build & push the operator container image to your Dockerhub/Quay image repository, e.g.

`operator-sdk build $IMAGE --enable-tests && docker push $IMAGE`

Run the test 

`operator-sdk test cluster $IMAGE --namespace $NAMESPACE --service-account unifiedpush-operator`

### Publishing Images

Images are automatically built and pushed to our [image repository](https://quay.io/repository/aerogear/unifiedpush-operator) by the Jenkins in the following cases:

- For every change merged to master a new image with the master tag is published.
- For every change merged that has a git tag a new image with the <operator-version> and latest tags are published.


### Tag Releases 

These are the steps for carrying out tag releases 

1. Create a new version tag following the [semver](https://semver.org/spec/v2.0.0.html) eg 0.1.0

2. Bump the version in the [version.go](https://github.com/aerogear/unifiedpush-operator/blob/master/version/version.go) file.

3. Update the [CHANGELOG.MD](https://github.com/aerogear/unifiedpush-operator/blob/master/CHANGELOG.md) with the new release.

4. Update any tag references in all SOP files (e.g https://github.com/aerogear/unifiedpush-operator/blob/0.1.0/SOP/SOP-operator.adoc ) 

5. Create a git tag with the version value. 

   `$ git tag -a 0.1.0 -m "version 0.1.0"`

:::note
The image with the tag will be created and pushed to the unifiedpush-operator image hosting repository by the Jenkins.
:::

7. Create a release in Github so that it is picked up by some internal process.

### Architecture

This operator is `cluster-scoped`. For further information see the [Operator Scope](https://github.com/operator-framework/operator-sdk/blob/master/doc/user-guide.md#operator-scope) section in the Operator Framework documentation. Also check its roles in [Deploy](https://github.com/aerogear/unifiedpush-operator/blob/master/deploy) directory


### CI/CD

#### CircleCI

 - Coveralls
 - Unit Tests

:::note 
see the [config.yml](https://github.com/aerogear/unifiedpush-operator/blob/master/.circleci/config.yml)
:::

#### Jenkins

 - Integration Tests
 - Build of images

:::note 
See the [Jenkinsfile](https://github.com/aerogear/unifiedpush-operator/blob/master/Jenkinsfile)
:::

### Makefile command reference

The following tables detail the makefile commands and are a handy point of reference for future use!

#### Application 

| Command | Description |
|---------|-------------|
|make install | Creates the {namespace} namespace, application CRDS, cluster role and service account. |
| make cluster/clean | It will delete what was performed in the make cluster/prepare. |
| make cluster/prepare | It will apply all less the operator.yaml. |

#### Local Development 

| Command | Description|
|---------|------------|
| make code/run | Runs the operator locally for development purposes |
| make code/gen | Sets up environment for debugging purposes |
| make code/vet | Examines source code and reports suspicious constructs using [vet](https://golang.org/cmd/vet/) |
| make code/fix | Formats code using [gofmt](https://golang.org/cmd/gofmt/) |

#### Jenkins

| Command | Description |
|---------|-------------|
| make test/compile | Compile image to be used in the e2e tests |
| make code/compile | Compile image to be used by the Jenkins |

#### Tests / CI
| Command | Description |
|---------|-------------|
| make test/integration-cover | It will run the coveralls |
| make test/unit | Runs unit tests |
| make code/build/linux | Build image with the parameters required for CircleCI |

:::note
The [Makefile](https://github.com/aerogear/unifiedpush-operator/blob/master/Makefile) is implemented with tasks which you should use to work with.

### Supportability 

The _UnifiefPush Server_ operator was developed using the Kubernetes and OpenShift APIs.

Currently this project requires the usage of the [v1.Route](https://docs.openshift.com/container-platform/3.11/rest_api/apis-route.openshift.io/v1.Route.html) to expose the service and [OAuth-proxy](https://github.com/openshift/oauth-proxy) for authentication which make it unsupportable for Kubernetes. In this way, this project is not compatible with Kubernetes, however, in future we aim to make it work on vanilla Kubernetes also.
