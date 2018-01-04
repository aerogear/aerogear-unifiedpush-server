FROM docker.io/jboss/keycloak-openshift:latest

ENV KEYCLOAK_ADMIN_USER "admin"
ENV KEYCLOAK_ADMIN_PASS "password"
ENV KEYCLOAK_ADMIN_HOST "http://keycloak.192.168.37.1.nip.io/auth"

ADD   https://raw.githubusercontent.com/aerogear/aerogear-unifiedpush-server/master/docker-compose/keycloak-realm/ups-realm-sample.json realm.json
COPY  job.sh job.sh
USER root
RUN chown jboss:jboss job.sh
RUN chmod +777 job.sh
RUN chmod +777 realm.json
RUN chown jboss:jboss realm.json
RUN mkdir -p ?/.keycloak
RUN chown jboss:jboss ?/.keycloak
RUN chmod +777 ?/.keycloak

USER jboss
ENTRYPOINT [ "./job.sh" ]
