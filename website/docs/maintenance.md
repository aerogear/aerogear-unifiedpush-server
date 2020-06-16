---
id: maintenance
title: Maintaining Unified Push
sidebar_label: Maintaining Unified Push
---

## Backup
 
 All the data required to run the _UnifiedPush Server_ is stored inside its database instance so that's all you need to backup. Currently supported databases are:
 * PostgreSQL
 * MySQL
 
 Since the _UnifiedPush Server_ runs on **WILDFLY** we can easily get information about the database by inspecting the _standalone.xml_ file:
 
<!--DOCUSAURUS_CODE_TABS-->
<!--UnifiedPush running in docker-->
If you are running _UnifiedPush Server_ from one of the provided docker images, the server is installed into the `opt` folder.

To get the content of the `standalone.xml` file, first we need to get the _docker container id_, then we must ask the content of the file
to docker:

```bash
CONTAINER_ID={YOUR CONTAINER ID}
READ_CONF="docker exec -it $CONTAINER_ID cat /opt/jboss/wildfly/standalone/configuration/standalone.xml"
bash -c "$READ_CONF"
```

The container must be up and running for this to work.  
To get the **CONTAINER_ID**, use the `docker ps` command and search for the _UnifiedPush_ container.


<!--UnifiedPush running standalone-->
 If you are running _UnifiedPush Server_ standalone in your own **WILDFLY instance**, then we just need to jump into the 
 _WILDFLY home_ to read the `standalone.xml` file:
 
```bash
WILDFLY_HOME=/path/to/your/wildfly
READ_CONF="cat $WILDFLY_HOME/standalone/configuration/standalone.xml"
bash -c "$READ_CONF"
```
<!--END_DOCUSAURUS_CODE_TABS-->
 
 :::note
 We assigned the command to the `READ_CONF` variable because it will comes handy to use it later. The command can, however, be run directly.
 :::
 
 Now that we have the configuration file, we can get all the connection details. You can manually search for a _datasource_ named
 **UnifiedPushDS** or you can use a tool like `xmlstarlet`:
 
 * **Connection URL**:
   ```bash
   CONNECTION_URL=`bash -c "$READ_CONF" | xmlstarlet sel -N x="urn:jboss:domain:datasources:5.0" -t -m "//x:datasource[@pool-name='UnifiedPushDS']//x:connection-url/text()" -c .`
   ```
 * **Username**:
   ```bash
   DB_USERNAME=`bash -c "$READ_CONF" | xmlstarlet sel -N x="urn:jboss:domain:datasources:5.0" -t -m "//x:datasource[@pool-name='UnifiedPushDS']//x:user-name/text()" -c .`
   ```
 * **Password**:
   ```bash
   DB_PASSWORD=`bash -c "$READ_CONF" | xmlstarlet sel -N x="urn:jboss:domain:datasources:5.0" -t -m "//x:datasource[@pool-name='UnifiedPushDS']//x:password/text()" -c .`
   ```
 Values are now stored into the **CONNECTION_URL**, **DB_USERNAME** and **DB_PASSWORD** environment variables:
 ```bash
 printf "CONNECTION_URL=$CONNECTION_URL \nUSERNAME=$DB_USERNAME \nPASSWORD=$DB_PASSWORD\n" 
 ```

For detailed instructions on how to backup the database, look at the official documentation"
* MySQL: https://dev.mysql.com/doc/refman/8.0/en/backup-and-recovery.html
* PostegreSQL: https://www.postgresql.org/docs/12/backup.html
 