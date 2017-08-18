# AeroGear Unified Push Server - Admin UI [![Build Status](https://travis-ci.org/aerogear/aerogear-unifiedpush-server-admin-ui.png)](https://travis-ci.org/aerogear/aerogear-unifiedpush-server-admin-ui)

### Setting Up The Development Environment

**Server Pre-Reqs (skip DB setup if not needed):**
* ### DB Setup:
* Install docker compose (if you have docker engine installed it should be included in that, run `docker-compose --version` to check)
* cd into `aerogear-unifiedpush-server/databases/docker`
* To run the DBs run `docker-compose -f docker-compose.yml up`
* Now run `mvn clean install` (need to have maven installed (and jdk setup for maven too))

---

* ### Server Setup:
* Download jboss server [here](https://s3-eu-west-1.amazonaws.com/fh-builds/eap/jboss-eap-6.4.15.zip)
* Copy over the `auth-server` war file into the jboss server => `cp aerogear-unifiedpush-server/servers/auth-server/target/auth-server.war /JBOSS_DIR/standalone/deployments/`
* For the ag-push war file you need the exploded (unarchived) war file for live reload to work so copy over the ag-push directory and name it ag-push.war => `cp -r aerogear-unifiedpush-server/servers/ups-as7/target/ag-push /JBOSS_DIR/standalone/deployments/ag-push.war`
* To run the server cd into `/JBOSS_DIR/` and run `./bin/standalone.sh --server-config=standalone-full.xml`
* Configure the JMS destination for UnifiedPush Server [here](https://aerogear.org/docs/unifiedpush/ups_userguide/index/#confjms)
* Generate the UnifiedPush Database and Datasource for mySQL [here](https://aerogear.org/docs/unifiedpush/ups_userguide/index/#gendbds) (Only do steps 1, 2, 4 ) 
Note: In step 1, make sure you don’t have an extra slash at the end of `/com`
* Update `mysql-database-config.cli` (found in `aerogear-unifiedpush-server/databases`) to use the url and ports of your dbs running in docker. 
* Now continue to step 5 (the eap script).
* The UnifiedPush Server Console can now be accessed at `http://localhost:8080/ag-push/`
* To login `username:admin` and `password:123` (it will prompt you to change password on first login)

---

**Admin-UI Pre-Reqs:**
* Node.js - [Download Here](http://nodejs.org/)
* Bower( version >= 0.9.1 ) - [Download Here](http://bower.io/)

Once the pre-reqs have been taken care of, cd into `Admin-UI` and run:

    npm install -g grunt-cli bower
    
    npm install

    bower install


Run:

    grunt initLocalConfig

Now you need to modify `admin-ui/local-config.json` file and fill in `jbossweb` property properly:

    {
        "home": "/home/sebastien/aerogear/aerogear-unified-push-server-admin-ui",
        "jbossweb": "/home/sebastien/apps/jboss-as-7.1.1.Final/standalone/deployments/ag-push.war",
    };
    
_note:  This is now required for all grunt steps below because of bug in assemble-less - once fixed, it will be required just for development_

To run:

    grunt server
    
Now everytime you save a file, grunt is watching and will copy to configured directories.


### Generate distribution

To create a distribution in `admin-ui/dist/` directory:

    grunt dist
    
To create a distribution and copy it jbossweb folder (as configured above):

    grunt jbosswebDist
    
    
In order to create a WAR application, go into `../server/` folder and run Maven build:

    cd ../server/
    mvn clean install
    

### Cleaning the Admin UI build

For sake of quick development turnaround, the `$ mvn clean` will clean just `dist/` and `.tmp/` build directories, but some frontend build related directories will be still cached (`node/`, `node_modules/`, `app/bower_components/`, `.build-tmp`). In order to clean all build related caches, execute:

    mvn clean install -Dfrontend.clean.force


### Managing NPM packages

The versions of packages listed in `package.json` and their transitive dependencies has to be locked down leveraging [NPM Shrinkwrap tool](http://blog.nodejs.org/2012/02/27/managing-node-js-dependencies-with-shrinkwrap/) (standard part of NPM distribution).

Use of [semantic versioning](https://github.com/npm/node-semver) in NPM makes Node module versions resolution in `package.json` undeterministic. `npm-shrinkwrapp.json` is an equivalent of `package.json` that locks down all the transitive dependencies.

#### Use of shrink-wrapped NPM configuration

For final user, nothing changes:

    npm install

You just need to be aware that `npm-shrinkwrap.json` configuration takes precedence.

#### Upgrading dependencies

The biggest change comes with changing dependency versions, since simple change of `package.json` won't have any effect. In order to upgrade a package, you can use approach like following one:

    $ npm install <package>@<version> --save--dev

Test the build to verify that the new versions work as expected

To lock down version again:

    $ npm shrinkwrap --dev
    $ git add package.json npm-shrinkwrap.json
    $ git commit -m "upgrading <package> to <version>"

Alternatively, you can remove `npm-shrinkwrap.json` and generate a new one.

### Build errors

The `frontend-maven-plugin` build may suffer from inconsistent downloads when you killed the previous build prematurely. This typically leads to such errors:

    [INFO] --- frontend-maven-plugin:0.0.16:grunt (grunt build) @ unifiedpush-admin-ui ---
    [INFO] Running 'grunt dist --no-color'
    [INFO] module.js:340
    [INFO]     throw err;
    [INFO]           ^
    [INFO] Error: Cannot find module 'findup-sync'

or

    [INFO] --- frontend-maven-plugin:0.0.16:npm (npm install) @ unifiedpush-admin-ui ---
    [INFO] Running 'npm install --color=false'
    [INFO] npm ERR! cb() never called!
    [INFO] npm ERR! not ok code 0

The build currently can't recover itself from these error.

In order to fix this issue, you should fully clean the `admin-ui/` build resources:

    mvn clean install -Dfrontend.clean.force
