# UnifiedPush Server in OpenShift

Make sure you have at least two 1G persistent volumes provisioned, then create a new project:

```bash
$ oc new-project ups
```

UPS Templates for OpenShift comes in 2 flavors:
* `ups-template.json` is there for deploying UPS components only and relies on an existing Keycloak instance,
* `ups-keycloak-template.yaml` is a convenient template that deploys UPS and a new Keycloak instance within the same OpenShift project.

Create the OpenShift UPS application using chosen template with parameters:

```bash
$ oc new-app -f ups-template.json --param=NAME=VALUE [...] -n ups
```

Or register template within namespace for a later instanciation of components through OpenShift console:

```bash
$ oc create -f ups-template.json  -n ups
```

Run `oc get pods -w` and monitor until all pods are in the `Running` state, and you're good to go.
The UnifiedPush server should be available at URL your specify with `UPS_URL` explained below.

Databases user and password settings are stored within Secrets into OpenShift.

## Common parameters

These are the common mandatory parameters for the templates:
* `UPS_URL` is the URL of UPS as reachable through the OpenShift route. Example: `ups-<project>.apps.domain.com`
* `KEYCLOAK_HOSTNAME` is the hostname of Keycloak instance to use. Example: `keycloak-<project>.apps.domain.com`
* `KEYCLOAK_PORT` is the port of Keycloak route. `80` for HTTP route (not production ready!), `443` for HTTPS route.

These are the common optional parameters for the templates that can overwritten:
* `UPS_IMAGE` references the container image used for UPS server.
* `UPS_DB_SIZE` is the default size of PV claim for UPS database (1Gi is the default).
* `MYSQL_IMAGE` references the container image used for database.

## Specific parameters for Keycloak template

These are optional parameters for the complete template:
* `KEYCLOAK_IMAGE` references the container image used for Keycloak server.
* `KEYCLOAK_DB_SIZE` is the default size of PV claim for Keycloak database (1Gi is the default).
