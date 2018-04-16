#!/bin/bash

/opt/jboss/wildfly/bin/standalone.sh -b 0.0.0.0 -Dkeycloak.import=/opt/jboss/ups-realm-sample.json
