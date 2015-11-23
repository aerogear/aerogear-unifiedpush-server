# UnifiedPush Server - Migration guide

For the database migration you may need to run a simple shell script.

# 1.0.x users

For the migration to our 1.0.3 release we are only supporting the 1.0.x series of the UnifiedPush Server:

* 1.0.0
* 1.0.1
* 1.0.2

In terms of supported databases, we are supporting MySQL 5.5 and PostgreSQL 9. 

## Getting started

Extract the `unifiedpush-migrator-dist.zip` file to your desired location. 

_NOTE:_ It is recommended to shutdown the application server, while performing the migration.

### Database Migration

Below you find information on how to perform the migration on each of the supported databases.

#### PostgreSQL

Inside of the extracted zip file, there is a `liquibase-postgresql-example.properties`, copy that to `liquibase.properties`.

    cp liquibase-postgresql-example.properties liquibase.properties

Once done, you need to edit the new file to match your database name and credentials.

#### MySQL

Inside of the extracted zip file, there is a `liquibase-mysql-example.properties`, copy that to `liquibase.properties`.

    cp liquibase-mysql-example.properties liquibase.properties

Once done, you need to edit the new file to match your database name and credentials.

#### Run the script

After the `liquibase.properties` contains the proper credentials, you need to execute the migration tool:

    ./bin/ups-migrator update

In case of a successful run, the script prints

    Liquibase Update Successful


If you want to get some more details on the actual migration steps, enable logging while performing the database migration:

    ./bin/ups-migrator --logLevel=DEBUG update
    
#### Running the script from source

If you are developing UnifiedPush Server, you can run the script from source instead of from distribution:

    cd aerogear-unifiedpush-server/migrator/
    mvn clean install dependency:copy-dependencies -DincludeScope=runtime
    export UPS_MIGRATOR_HOME=$PWD/target/dependency/*:$PWD/target
    sh ./src/main/shell/ups-migrator update

### WAR migration

After successfully executing the above database migration script you need to replace the deployed 1.0.x WAR files with the new ones and start the application server.

That's it :smile:

In case of any error, please contact us on our [mailing list](https://github.com/aerogear/aerogear-unifiedpush-server#project-info)!
