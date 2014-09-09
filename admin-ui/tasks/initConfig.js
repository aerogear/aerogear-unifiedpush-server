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
                jbossweb: "<PATH TO YOUR JBOSS/WILDFLY DIRECTORY>/standalone/deployments/ag-push.war"
            }
            grunt.file.write('./local-config.json',JSON.stringify(sampleContent,null,'\t'));
            grunt.fatal('please update local-config.json with the path to your application server');
        }
        var config = grunt.config.getRaw();
        config.local = grunt.file.readJSON('./local-config.json');
        
        verifyJBosswebDirectory(config.local.jbossweb);
    });
    
    function verifyJBosswebDirectory(jbossweb) {
        if (!grunt.file.exists(jbossweb)) {
            grunt.fatal('jbossweb directory ' + jbossweb + ' configured in ./local-config.json does not exist, please deploy the exploded WAR first');
        }
        if (!grunt.file.isDir(jbossweb)) {
            grunt.fatal('jbossweb directory ' + jbossweb + ' configured in ./local-config.json is not directory (must be exploded WAR)');
        }
        if (!grunt.file.exists(jbossweb + '/WEB-INF') || !grunt.file.isDir(jbossweb + '/WEB-INF')) {
            grunt.fatal('jbossweb directory ' + jbossweb + ' configured in ./local-config.json does not contain directory ./WEB-INF (is it really an exploded WAR?)');
        }
        if (!/\/standalone\/deployments\//.test(jbossweb)) {
            grunt.fatal('jbossweb directory ' + jbossweb + ' configured in ./local-config.json does not have parent directories /standalone/deployments/, are you sure it is a correct path to the deployed exploded WAR?');
        }
    }


};