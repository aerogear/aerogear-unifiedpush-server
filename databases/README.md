Liquibase database migrations project
=====================================

This project takes care of migrating database changes. When database changes are needed create a file `db.changelog-<version>.xml` and add it to the master (`db.changelog-master.xml`)

Run the migrations
==================
Change the database settings in the pom.xml and run `mvn liquibase:update -P<database>` where `<database>` is either `mysql` or `postgres`