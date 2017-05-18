#!/bin/bash

# Function for usage instructions
function usage()
{
cat << EOF >&2

Setup Keycloak for usage with UnifiedPush Server

Before you begin:

    Please make sure to have WILDFLY_HOME environment variable properly configured

    Example: export WILDFLY_HOME=/path/to/your/keycloak/server/installation
             export JBOSS_HOME=$WILDFLY_HOME (not mandatory)

Usage:
    $(basename $0) [options]

Example:
    $(basename $0) --ups-host=https://ups-host:8083 --realm-import=ups-realm-template.json

Options:
    -s, --ups-host          UnifiedPush HTTP server host
    -i, --realm-import      Import Realm file
    -o, --offset            Port offset from WildFly
    -h, --help              Help
EOF
}

# read the options
TEMP=`getopt -o s:o:i: --long ups-host:,wildfly-offset:,realm-import: -n 'keycloak-setup.sh' -- "$@"`
eval set -- "$TEMP"

if [ $# -eq 1 ] ; then
    usage;
    exit 1
fi

# extract options and their arguments into variables.
while true ; do
    case "$1" in
        -s|--ups-host)
          case "$2" in
            "") shift 2 ;;
                *) UPS_HOST=$2 ; shift 2;;
          esac ;;
        -o|--wildfly-offset)
          case "$2" in
            "") shift 2 ;;
                *) WILDFLY_OFFSET=$2 ; shift 2;;
          esac ;;
        -i|--realm-import)
          case "$2" in
            "") shift 2 ;;
                *) REALM_JSON_FILE=$2 ; break;;
          esac ;;

      --) shift; break;;
      *) "Internal error!"; echo $1 ; usage; exit 1 ;;

    esac
done

if [[ ! -z "$UPS_HOST" && ! -z "$REALM_JSON_FILE" ]]; then

    IFS=,
    UPS_HOST_ARRAY=($UPS_HOST)
    JSON_FILE_ARRAY=($REALM_JSON_FILE)

    for key in "${!UPS_HOST_ARRAY[@]}"; do
      sed -i "s|dummyhost|${UPS_HOST_ARRAY[$key]}|g" ${JSON_FILE_ARRAY[$key]}
    done

    IFS=

    if [ ! "$WILDFLY_OFFSET" ]; then
        $WILDFLY_HOME/bin/standalone.sh -Dkeycloak.import=$REALM_JSON_FILE
    else
        $WILDFLY_HOME/bin/standalone.sh -Djboss.socket.binding.port-offset=$WILDFLY_OFFSET -Dkeycloak.import=$REALM_JSON_FILE
    fi
else
    usage;
fi


