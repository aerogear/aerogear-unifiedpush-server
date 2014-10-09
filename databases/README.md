Liquibase database migrations project
=====================================

This project takes care of migrating database changes. When database changes are needed create a file `db.changelog-<version>.xml` and add it to the master (`db.changelog-master.xml`)

To update the database to the new schema run `mvn liquibase:update`

More information about liquibase see http://www.liquibase.org/