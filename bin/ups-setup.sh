#!/bin/bash

# Function for usage instructions
function usage()
{
cat << EOF >&2

Setup UnifiedPush Server for usage with Keycloak

Before you begin:

    Please make sure to have WILDFLY_HOME environment variable properly configured

    Example: export WILDFLY_HOME=/path/to/your/wildfly/ups/server/installation
             export JBOSS_HOME=$WILDFLY_HOME (not mandatory)

Usage:
    $(basename $0) [options]

Example:
    $(basename $0) --subsystem-setup

    $(basename $0) --ups-host=localhost:9992 --realm=aerogear --auth-server=http://localhost:8083

Options:
    -s, --ups-host          UnifiedPush server controller host
    -r, --realm             Realm name
    -k, --auth-server       Keycloak server
        --subsystem-setup   Keycloak subsystem setup
    -h, --help              Help
EOF
}

# Function for Keycloak subsystem setup (optional)
function subsystem_setup()
{
    KEYCLOAK_WILDFLY_ADAPTER="keycloak-wildfly-adapter-dist-1.7.0.Final"
    if [ ! "$WILDFLY_HOME" ]; then
        printf "Error: Please configure the installation path for WildFly\n"
        printf "   >>> Ex: WILDFLY_HOME=/path/to/your/wildfly-ups-installation\n"
    else
        cd $WILDFLY_HOME
        curl -O "http://downloads.jboss.org/keycloak/1.7.0.Final/adapters/keycloak-oidc/$KEYCLOAK_WILDFLY_ADAPTER.zip"
        unzip $KEYCLOAK_WILDFLY_ADAPTER
    fi
    exit 0
}

# read the options
TEMP=`getopt -o s:r:k:h --long ups-host:,realm:,auth-server:,help,subsystem-setup -n 'ups-setup.sh' -- "$@"`
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
                *) UPS_HOST=$2 ; shift 2 ;;
          esac ;;

        -r|--realm)
          case "$2" in
            "") shift 2 ;;
                *) REALM_NAME=$2; shift 2 ;;
          esac ;;

        -k|--auth-server)
          case "$2" in
            "") shift 2 ;;
                *) AUTH_SERVER=$2; shift 2 ;;
          esac ;;

        --subsystem-setup)
          subsystem_setup; break;;

        -h|--help)
          usage; shift;;

      --) shift ; break ;;
      *) "Internal error!"; usage; exit 1 ;;
    esac
done

if [[ ! -z "$UPS_HOST" && ! -z "$REALM_NAME" && ! -z $AUTH_SERVER ]]; then

    $WILDFLY_HOME/bin/jboss-cli.sh -c --controller=$UPS_HOST --command="/system-property=ups.auth.server.url:add(value=$AUTH_SERVER/auth)";
    $WILDFLY_HOME/bin/jboss-cli.sh -c --controller=$UPS_HOST --command="/system-property=ups.realm.name:add(value=$REALM_NAME)"

else
    usage;
fi




