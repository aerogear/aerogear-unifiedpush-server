#!/bin/bash
set -e
SCRIPTDIR=`dirname $0`

if [ ! "$WILDFLY_HOME" ]; then
    printf "Error: Please configure the installation path for WildFly\n"
    printf "   >>> Ex: WILDFLY_HOME=/path/to/your/wildfly11-installation\n"
    exit 1
else
    printf "😂 Awesome, WildFly 11 is configured!\n"
    printf "   >>> Preparing the PostgreSQL DB module\n"
    cp -r $SCRIPTDIR/src/main/resources/modules/org $WILDFLY_HOME/modules
    mvn dependency:copy -Dartifact=org.postgresql:postgresql:42.2.6 -DoutputDirectory=$WILDFLY_HOME/modules/org/postgresql/main/
    printf "WildFly 11 DB configuration is about to start\n"
    printf "   >>> Running the jboss-cli tool!\n"
    $WILDFLY_HOME/bin/jboss-cli.sh --file=$SCRIPTDIR/postgresql-database-config-wildfly.cli

    printf "WildFly 11 JMS configuration is about to start\n"
    printf "   >>> Running the jboss-cli tool!\n"
    $WILDFLY_HOME/bin/jboss-cli.sh --file=$SCRIPTDIR/../configuration/jms-setup-wildfly.cli

    printf "Huzza, it worked!\n"

fi
exit 0
