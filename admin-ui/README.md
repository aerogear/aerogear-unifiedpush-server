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


#### Generate distribution

To create a distribution in `admin-ui/dist/` directory:

    grunt
    
To create a distribution and copy it to `../server/target/ag-push`:

    grunt dist
    
To create a distribution and copy it jbossweb folder (as configured above):

    grunt jbosswebDist
    
    
In order to create a WAR application, go into `../server/` folder and run Maven build:

    cd ../server/
    mvn clean install
    
By default, the "$ mvn clean" will delete all previously downloaded node.js related directories. If you want to make your build faster and
not download same packages over again, please use profile intended for this purpose:

    mvn clean install -Pdev
