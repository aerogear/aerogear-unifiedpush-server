# Use latest jboss/wildfly image as the base
FROM ${base}
MAINTAINER Matthias Wessendorf <matzew@apache.org>

# Run everything below as root user
USER root

# Clean the metadata
RUN yum install -y unzip wget && yum -q clean all

ARG CACHEBUST=1

# Run everything below as aerogear user
USER jboss

# WildFly configuration file ready for JMS and HSQL
ADD ${configFile} $JBOSS_HOME/standalone/configuration/standalone.xml

# WildFly configuration file ready for JMS and HSQL
ADD ups-realm-sample.json /opt/jboss/ups-realm-sample.json

# Switch to the working dir /opt/jboss/wildfly
WORKDIR /opt/jboss/wildfly

# WildFly configuration file ready for JMS and HSQL
COPY keycloak-overlay-3.4.3.Final.tar.gz $JBOSS_HOME
RUN tar xf keycloak-overlay-3.4.3.Final.tar.gz

# Switch to the working dir $JBOSS_HOME/standalone/deployments
WORKDIR /opt/jboss/wildfly/standalone/deployments

# add war files
COPY maven/ $JBOSS_HOME/standalone/deployments
RUN mv $JBOSS_HOME/standalone/deployments/ROOT.war $JBOSS_HOME/standalone/deployments/ag-push.war

# Expose default port
EXPOSE 8080

COPY entrypoint.sh /opt/
ENTRYPOINT ["/opt/entrypoint.sh"]
