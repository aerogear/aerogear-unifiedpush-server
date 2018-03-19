# Use latest jboss/wildfly image as the base
FROM jboss/wildfly:12.0.0.Final
MAINTAINER Matthias Wessendorf <matzew@apache.org>

# Run everything below as root user
USER root

# Clean the metadata
RUN yum install -y unzip wget && yum -q clean all

# WildFly configuration file ready for JMS and HSQL
ADD configuration/standalone-full.xml $JBOSS_HOME/standalone/configuration/standalone.xml

# Switch to the working dir /opt/jboss/wildfly
WORKDIR /opt/jboss/wildfly

# Run everything below as aerogear user
USER jboss

# Switch to the working dir $JBOSS_HOME/standalone/deployments
WORKDIR /opt/jboss/wildfly/standalone/deployments

# add war files
ADD servers/target/ag-push.war $JBOSS_HOME/standalone/deployments

# Expose default port
EXPOSE 8080
