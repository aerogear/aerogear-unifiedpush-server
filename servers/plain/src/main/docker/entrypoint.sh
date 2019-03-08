#!/bin/bash

set -e

# launch wildfly
echo "launching wildfly"
exec $JBOSS_HOME/bin/standalone.sh -b 0.0.0.0 $@
