#!/bin/bash

if [ ! "$WILDFLY_HOME" ]; then
    printf "Error: Please configure the installation path for WildFly\n"
    printf "   >>> Ex: WILDFLY_HOME=/path/to/your/wildfly11-installation\n"
    exit 1
else
    printf "😂 Awesome, WildFly 11 is configured!\n"
    printf "   >>> Preparing the MySQL DB module\n"
    cp -r ./src/main/resources/modules/com $WILDFLY_HOME/modules
    mvn dependency:copy -Dartifact=mysql:mysql-connector-java:8.0.15 -DoutputDirectory=$WILDFLY_HOME/modules/com/mysql/jdbc/main/  
    printf "WildFly 11 DB configuration is about to start\n"
    printf "   >>> Running the jboss-cli tool!\n"
    $WILDFLY_HOME/bin/jboss-cli.sh --file=./mysql-database-config-wildfly.cli

    printf "WildFly 11 JMS configuration is about to start\n"
    printf "   >>> Running the jboss-cli tool!\n"
    $WILDFLY_HOME/bin/jboss-cli.sh --file=../configuration/jms-setup-wildfly.cli

    printf "Huzza, it worked!\n"

fi
exit 0
