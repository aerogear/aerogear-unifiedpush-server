FROM ${base}
MAINTAINER AeroGear <aerogear@googlegroups.com>

USER root

RUN yum install -y unzip wget && yum -q clean all

ARG CACHEBUST=1

ENV psql_module_dir=$JBOSS_HOME/modules/org/postgresql/main/
ENV psql_connector_jar=postgresql-jdbc.jar
RUN mkdir -p ${psql_module_dir}
RUN wget -O ${psql_connector_jar} http://search.maven.org/remotecontent\?filepath\=org/postgresql/postgresql/42.2.2/postgresql-42.2.2.jar
RUN mv ${psql_connector_jar} ${psql_module_dir}

ADD ${postgresConfigFile} ${psql_module_dir}/module.xml

ADD ${configFile} $JBOSS_HOME/standalone/configuration/standalone.xml

USER jboss

WORKDIR $JBOSS_HOME/standalone/deployments

COPY maven/ $JBOSS_HOME/standalone/deployments

COPY entrypoint.sh /opt/
ENTRYPOINT ["/opt/entrypoint.sh"]
