# UnifiedPush Server - Database migration

For the database migration you may need to run a few scripts inside of the database

## 1.0.0 users

The UnifiedPush Server is supporting two different databases, MySQL and PostgreSQL, below you find information on how to perform the migration on each of the supported databases

### MySQL

Inside of the database execute these two scripts:

* `keycloak-migration-mysql.sql`
* `ups-migration-mysql.sql`

### PostgreSQL

Inside of the database execute these two scripts:

* `keycloak-migration-postgresql.sql`
* `ups-migration-postgresql.sql`

### WAR files

After successfully executing the above migration scripts you need to replace the deployed 1.0.0 WAR files with the new ones. That's it!

## 1.0.1 or 1.0.2 users

No migration is required :smile:
