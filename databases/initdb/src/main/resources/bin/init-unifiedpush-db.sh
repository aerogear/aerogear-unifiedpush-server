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

#remote debug parameters
#export DEBUG_OPTS="-Xdebug -Xrunjdwp:transport=dt_socket,address=1044,server=y,suspend=y"

while [ -n "$1" ]; do
    v="${1#*=}"
    case "$1" in
        --database=*)
            export DATABASE="${v}"
            ;;
        --config-path=*)
            export CONFIG="${v}"
            ;;
        --help|*)
                cat <<__EOF__
Usage: $0
        --database=database - Database name
        --config-path=path  - Path for -Daerogear.config.dir param - Default /tmp/db.properties
__EOF__
        exit 1
    esac
    shift
done

[ -z "${DATABASE}" ] && die "Missing database! Usage: ./init-unifiedpush-db.sh database name"
[ -z "${CONFIG}" ] && export CONFIG=/tmp/db.properties

${JAVA_HOME}/bin/java ${DEBUG_OPTS} \
        -Daerogear.config.dir=${CONFIG} \
        -Dorg.jboss.aerogear.unifiedpush.initdb.database=${DATABASE} \
        -cp "../lib/*" \
        org.jboss.aerogear.unifiedpush.DBMaintenance \
        "${DATABASE}"