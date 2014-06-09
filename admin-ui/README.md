# AeroGear Unified Push Server - Admin UI [![Build Status](https://travis-ci.org/aerogear/aerogear-unifiedpush-server-admin-ui.png)](https://travis-ci.org/aerogear/aerogear-unifiedpush-server-admin-ui)

### Setting Up The Development Environment

Pre-reqs:

* Node.js - [Download Here](http://nodejs.org/)
* Bower( version >= 0.9.1 ) - [Download Here](http://bower.io/)

Once the pre-reqs have been taken care of, run:

    npm install -g bower-cli
    
    npm install

    bower install


Run:

    grunt initLocalConfig

Now you need to modify `admin-ui/local-config.json` file and fill in `jbossweb` property properly:

    {
        "home": "/home/sebastien/aerogear/aerogear-unified-push-server-admin-ui",
        "webapp": "../server/src/main/webapp",
        "jbossweb": "/home/sebastien/apps/jboss-as-7.1.1.Final/standalone/deployments/ag-push.war",
    };
    
_note:  This is now required for all grunt steps bellow because of bug in assemble-less - once fixed, it will be required just for development_

To run:

    grunt server
    
Now everytime you save a file, grunt is watching and will copy to configured directories.


#### Generate distribution

To create a distribution in `admin-ui/dist/` directory:

    grunt
    
To create a distribution and copy it to `../server/src/main/webapp`:

    grunt dist