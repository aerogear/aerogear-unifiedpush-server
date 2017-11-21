#!/bin/bash
/opt/jboss/keycloak/bin/kcadm.sh config credentials --server $KEYCLOAK_ADMIN_HOST --realm master --user $KEYCLOAK_ADMIN_USER --password $KEYCLOAK_ADMIN_PASS
/opt/jboss/keycloak/bin/kcadm.sh create realms -f /opt/jboss/realm.json
