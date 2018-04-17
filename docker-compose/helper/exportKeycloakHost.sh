#!/bin/sh

## ping kecyloak host and extract IP address, to be used in KEYCLOAK_SERVICE_URL
## this is needed since the UPS docker host needs to talk to the actual KC docker host,
## hence the actual IP is needed
export KEYCLOAK_SERVICE_HOST=$(ping -c 1 keycloak | grep -Eo -m 1 '[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}';)
echo KEYCLOAK runs on: $KEYCLOAK_SERVICE_HOST

sh /opt/entrypoint.sh
