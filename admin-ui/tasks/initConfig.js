'use strict';
var exec = require('child_process').exec;
var path = require('path');



module.exports = function ( grunt ) {

    /**
     * Task that loads the local config and merged it with the global config object
     * If the local config file does not exist it will be created.
     * Note : the local-config file is not meant to be in source control
     */
    grunt.registerTask( 'initLocalConfig',function(){
        if(!grunt.file.exists('./local-config.json')){
            var parentDir = path.resolve(process.cwd(), '.');
            var sampleContent = {
                home: parentDir,
                webapp: "../server/target/ag-push",
                jbossweb: "<PATH TO YOUR JBOSS/WILDFLY DIRECTORY>/standalone/deployments/ag-push.war"
            }
            grunt.file.write('./local-config.json',JSON.stringify(sampleContent,null,'\t'));
            grunt.fatal('please update local-config.json with the path to your application server');
        }
        var config = grunt.config.getRaw();
        config.local = grunt.file.readJSON('./local-config.json');
        grunt.log.ok( 'local config' + config.local.webapp );
    });


};