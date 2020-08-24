#!/bin/bash

#Export variables
printenv > $JBOSS_HOME/env.properties

#Explore ROOT.war
mkdir /opt/jboss/wildfly/standalone/deployments/ROOT
cd /opt/jboss/wildfly/standalone/deployments/ROOT/
mv /opt/jboss/wildfly/standalone/deployments/ROOT.war /opt/jboss/wildfly/standalone/deployments/ROOT
jar xf /opt/jboss/wildfly/standalone/deployments/ROOT/ROOT.war
rm -f /opt/jboss/wildfly/standalone/deployments/ROOT/ROOT.war

if [[ ! -z "${POSTGRES_SERVICE_HOST}" ]]; then
  echo "Postgres enabled"
  /opt/jboss/wildfly/bin/jboss-cli.sh --file=/opt/jboss/wildfly/bin/create-ups-postgres-ds.cli --properties=$JBOSS_HOME/env.properties
else
  echo "H2 in memory enabled"
  /opt/jboss/wildfly/bin/jboss-cli.sh --file=/opt/jboss/wildfly/bin/create-ups-h2-inmemory-ds.cli --properties=$JBOSS_HOME/env.properties
fi

if [ !  -z "${ARTEMIS_SERVICE_HOST}" ]; then
  echo "Remote AMQ"
  /opt/jboss/wildfly/bin/jboss-cli.sh --file=/opt/jboss/wildfly/bin/create-queues-with-artemis.cli --properties=$JBOSS_HOME/env.properties
  cp /opt/jboss/wildfly/bin/jboss-ejb3.xml.artemis /opt/jboss/wildfly/standalone/deployments/ROOT/WEB-INF/jboss-ejb3.xml
else
  echo "Local AMQ"
  /opt/jboss/wildfly/bin/jboss-cli.sh --file=/opt/jboss/wildfly/bin/create-queues-no-artemis.cli --properties=$JBOSS_HOME/env.properties
  cp /opt/jboss/wildfly/bin/jboss-ejb3.xml.noartemis /opt/jboss/wildfly/standalone/deployments/ROOT/WEB-INF/jboss-ejb3.xml
fi

if [ !  -z "${KEYCLOAK_SERVICE_HOST}" ]; then
  echo "Enabling KeyCloak"
  /opt/jboss/wildfly/bin/jboss-cli.sh --file=/opt/jboss/wildfly/bin/adapter-elytron-install-offline.cli --properties=$JBOSS_HOME/env.properties
else
  echo "No security"
  rm -f /opt/jboss/wildfly/standalone/deployments/ROOT/WEB-INF/keycloak.json
  cp /opt/jboss/wildfly/bin/web.xml.nokeycloak /opt/jboss/wildfly/standalone/deployments/ROOT/WEB-INF/web.xml
fi

jar -cf /opt/jboss/wildfly/standalone/deployments/ROOT.war -C /opt/jboss/wildfly/standalone/deployments/ROOT .
rm -rf /opt/jboss/wildfly/standalone/deployments/ROOT

cd -

if [ !  -z "${KEYCLOAK_SERVICE_HOST}" ]; then
  /opt/jboss/wildfly/bin/standalone.sh -Dups.realm.name=aerogear -Dups.auth.server.url=https://${KEYCLOAK_SERVICE_HOST}:${KEYCLOAK_SERVICE_PORT}/auth -b 0.0.0.0
else
  /opt/jboss/wildfly/bin/standalone.sh -b 0.0.0.0
fi


