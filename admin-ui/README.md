# AeroGear Unified Push Server - Admin UI [![Build Status](https://travis-ci.org/aerogear/aerogear-unifiedpush-server-admin-ui.png)](https://travis-ci.org/aerogear/aerogear-unifiedpush-server-admin-ui)

### Setting Up The Development Environment

Pre-reqs:

* Node.js - [Download Here](http://nodejs.org/)
* Bower( version >= 0.9.1 ) - [Download Here](http://bower.io/)

Once the pre-reqs have been taken care of, run:

    npm install -g grunt-cli bower
    
    npm install

    bower install


Run:

    grunt initLocalConfig

Now you need to modify `admin-ui/local-config.json` file and fill in `jbossweb` property properly:

    {
        "home": "/home/sebastien/aerogear/aerogear-unified-push-server-admin-ui",
        "webapp": "../server/target/ag-push",
        "jbossweb": "/home/sebastien/apps/jboss-as-7.1.1.Final/standalone/deployments/ag-push.war",
    };
    
_note:  This is now required for all grunt steps bellow because of bug in assemble-less - once fixed, it will be required just for development_

To run:

    grunt server
    
Now everytime you save a file, grunt is watching and will copy to configured directories.


### Generate distribution

To create a distribution in `admin-ui/dist/` directory:

    grunt
    
To create a distribution and copy it to `../server/target/ag-push`:

    grunt dist
    
To create a distribution and copy it jbossweb folder (as configured above):

    grunt jbosswebDist
    
    
In order to create a WAR application, go into `../server/` folder and run Maven build:

    cd ../server/
    mvn clean install
    
In sake of quick development turnaround, the `$ mvn clean` will clean just `dist/` and `.tmp/` build directories, but some frontend build related directories will be still cached (`node/`, `node_modules/`, `app/bower_components/`, `.bower-tmp`). In order to clean all build related caches, execute:

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
