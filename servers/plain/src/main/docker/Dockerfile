# Use latest jboss/wildfly image as the base
FROM ${base}
MAINTAINER Matthias Wessendorf <matzew@apache.org>

# Run everything below as root user
USER root

# Clean the metadata
RUN yum install -y unzip wget && yum -q clean all

ARG CACHEBUST=1

## PostgreSQL
ENV psql_module_dir=$JBOSS_HOME/modules/org/postgresql/main/
ENV psql_connector_jar=postgresql-jdbc.jar
RUN mkdir -p ${psql_module_dir}
RUN wget -O ${psql_connector_jar} http://search.maven.org/remotecontent\?filepath\=org/postgresql/postgresql/42.2.2/postgresql-42.2.2.jar
RUN mv ${psql_connector_jar} ${psql_module_dir}

ADD ${postgresConfigFile} ${psql_module_dir}/module.xml

# WildFly configuration file ready for JMS and HSQL
ADD ${configFile} $JBOSS_HOME/standalone/configuration/standalone.xml

# Run everything below as aerogear user
USER jboss

# Switch to the working dir $JBOSS_HOME/standalone/deployments
WORKDIR /opt/jboss/wildfly/standalone/deployments

# add war files
COPY maven/ $JBOSS_HOME/standalone/deployments

COPY entrypoint.sh /opt/
ENTRYPOINT ["/opt/entrypoint.sh"]
