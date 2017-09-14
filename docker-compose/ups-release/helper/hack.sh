#!/bin/sh

export KEYCLOAK_PORT_8080_TCP_ADDR=$(ping -c 1 keycloak | grep -Eo -m 1 '[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}';)
echo $KEYCLOAK_PORT_8080_TCP_ADDR

sh /opt/entrypoint.sh
