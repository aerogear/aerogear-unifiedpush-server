#!/bin/sh

export KEYCLOAK_SERVICE_HOST=$(ping -c 1 keycloak | grep -Eo -m 1 '[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}';)
echo KEYCLOAK runs on: $KEYCLOAK_SERVICE_HOST

sh /opt/entrypoint.sh
