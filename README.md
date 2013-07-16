## AeroGear Unified Push Server Admin UI

A WIP for the AeroGear Unified Push Server Admin UI

To install:

    npm install

    bower install


To run:

    grunt server

To create a distribtion:

    grunt



### Using With JBoss EAP/Wildfly

Clone and run [https://github.com/aerogear/aerogear-unified-push-server](unified push server) as an exploded war.

Then in "Gruntfile.js" edit the "webapp" and "jbossweb" config params:

     // configurable paths
    var yeomanConfig = {
        app: 'app',
        dist: 'dist',
        webapp: "/Users/lholmquist/develop/projects/aerogear-unified-push-server/src/main/webapp/admin",
        jbossweb: "/Users/lholmquist/develop/jboss-as-7.1.1.Final/standalone/deployments/ag-push.war/admin"
    };

This will copy the contents to the "admin" folder of both the cloned repo and the exploded war file( to keep them N'sync )

"webapp" should be the path where you cloned the push server.

"jbossweb" is the path of you exploded war running on an app server


_Make sure to rerun "grunt server" once you change these parameters_

Now everytime you save a file, grunt is watching and will copy to these directories

### Handlebar templates

This project uses handlebar templates( *.hbs )

Grunt is watching for changes in the "templates" directory and will recomplile "scripts/templates.js" with the new template
