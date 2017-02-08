#!/bin/bash

if [ -z "${JAVA_HOME}" ]; then
    # Gentoo
    if which java-config > /dev/null 2>&1; then
        export JAVA_HOME="$(java-config --jre-home)"
    else
        export JAVA_HOME="/usr"
    fi
fi

die() {
        local localmsg="$1"
        echo "FATAL: ${localmsg}" >&2
        exit 1
}

database="$1"
[ -z "${database}" ] && die "Missing database! Usage: ./init-cassandra-db.sh database name"

#remote debug parameters
#export DEBUG_OPTS="-Xdebug -Xrunjdwp:transport=dt_socket,address=1044,server=y,suspend=y"

# TODO - Support username password, e.g -u myusername -p mypassword localhost
# TODO - Use database parameter in .cql files.
cqlsh -f cassandra-keyspace.cql >> /tmp/inti-cassandra.log
cqlsh -f cassandra-tables.cql >> /tmp/inti-cassandra.log