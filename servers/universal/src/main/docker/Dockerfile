# Use latest jboss/wildfly image as the base
FROM ${base}
MAINTAINER Summers Pittman <supittma@redhat.com>

# Run everything below as root user
USER root

# Clean the metadata
RUN yum install -y unzip wget && yum -q clean all

ARG CACHEBUST=1

ENV psql_module_dir=$JBOSS_HOME/modules/org/postgresql/main/
ENV psql_connector_jar=postgresql-jdbc.jar
RUN mkdir -p ${psql_module_dir}
RUN wget -O ${psql_connector_jar} http://search.maven.org/remotecontent\?filepath\=org/postgresql/postgresql/42.2.6/postgresql-42.2.6.jar
RUN mv ${psql_connector_jar} ${psql_module_dir}

ADD config/psql-module.xml ${psql_module_dir}/module.xml

ADD standalone-full.xml $JBOSS_HOME/standalone/configuration/standalone.xml

# Run everything below as aerogear user
USER jboss

# Switch to the working dir /opt/jboss/wildfly
WORKDIR /opt/jboss/wildfly

# WildFly module
COPY keycloak-wildfly-adapter-dist-11.0.0.tar.gz $JBOSS_HOME
RUN tar xf keycloak-wildfly-adapter-dist-11.0.0.tar.gz
 #RUN ./bin/jboss-cli.sh --file=bin/adapter-elytron-install-offline.cli

# Switch to the working dir $JBOSS_HOME/standalone/deployments
WORKDIR /opt/jboss/wildfly/standalone/deployments

# add war files
COPY maven/ $JBOSS_HOME/standalone/deployments

COPY config/ $JBOSS_HOME/bin

RUN  sed -i "s/<resolve-parameter-values>false<\/resolve-parameter-values>/<resolve-parameter-values>true<\/resolve-parameter-values>/" $JBOSS_HOME/bin/jboss-cli.xml

# Expose default port
EXPOSE 8080

COPY entrypoint.sh /opt/
ENTRYPOINT ["sh","-c","/opt/entrypoint.sh"]
