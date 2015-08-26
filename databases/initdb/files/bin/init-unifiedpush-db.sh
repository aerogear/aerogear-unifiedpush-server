#!/bin/bash

if [ -z "${JAVA_HOME}" ]; then
    # Gentoo
    if which java-config > /dev/null 2>&1; then
        export JAVA_HOME="$(java-config --jre-home)"
    else
        export JAVA_HOME="/usr"
    fi
fi

database="$1"
[ -z "${database}" ] && die "Missing database! Usage: ./init-unifiedpush-db.sh database name"

#remote debug parameters
#export DEBUG_OPTS="-Xdebug -Xrunjdwp:transport=dt_socket,address=1044,server=y,suspend=y"

${JAVA_HOME}/bin/java ${DEBUG_OPTS} \
        -Dorg.jboss.aerogear.unifiedpush.initdb.database=${database} \
        -cp "../lib/*" \
        org.jboss.aerogear.unifiedpush.DBMaintenance \
        "${database}"