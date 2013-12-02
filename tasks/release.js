'use strict';
var exec = require('child_process').exec;
var semver = require('semver');

module.exports = function ( grunt ) {

    /**
     * Task that loads the local config and merged it with the global config object
     * If the local config file does not exist it will be created.
     * Note : the local-config file is not meant to be in source control
     */
    grunt.registerTask( 'initLocalConfig',function(){
        if(!grunt.file.exists('./local-config.json')){
            var sampleContent = {
                home: '<PATH TO THE CURRENT DIRECTORY>',
                webapp: "<PATH TO YOUR UPS REPO>/src/main/webapp",
                jbossweb: "<PATH TO YOUR JBOSS/WILDFLY DIRECTORY>/standalone/deployments/ag-push.war",
                ups_repo:"<PATH TO YOUR UPS REPO FOR RELEASE (IN CLEAN STATE)>"
            }
            grunt.file.write('./local-config.json',JSON.stringify(sampleContent,null,'\t'));
            grunt.fatal('please update local-config.json with your custom values');
        }
        var config = grunt.config.getRaw();
        config.local = grunt.file.readJSON('./local-config.json');
    });

    /**
     * This task does the following :
     *  - Update the package.json with release version (i.e: 0.10.0) and commit.
     *  - Create a git tag.
     *  - Calls 'bump' task
     */
    grunt.registerTask( 'tag', "tag and commit", function () {

        var done = grunt.task.current.async();
        var opts = this.options( {
            files: ['package.json'],
            commitFiles: ['package.json']
        } );
        var VERSION_REGEXP = /([\'|\"]version[\'|\"][ ]*:[ ]*[\'|\"])([\d||A-a|.|-]*)([\'|\"])/i;
        opts.files.forEach( function ( file, idx ) {
            var version = null;
            var content = grunt.file.read( file ).replace( VERSION_REGEXP, function ( match, prefix, parsedVersion, suffix ) {
                version = grunt.config( 'release' ).tagVersion
                return prefix + version + suffix;
            } );

            if ( !version ) {
                grunt.fatal( 'Can not find a version to bump in ' + file );
            }
            grunt.file.write( file, content );
        } );

        var commitLine = 'git commit ' + opts.commitFiles.join( ' ' ) + ' -m "prepare release"';
        grunt.log.ok( commitLine );
        exec( commitLine, function ( err, stdout, stderr ) {
            if ( err ) {
                grunt.log.ok(err);
                grunt.fatal( 'Can not create the commit for prepare:\n  ' + stderr );
            }
            grunt.log.ok( 'Commit prepare release' );
            //done(err);
            exec( 'git tag -a ' + grunt.config( 'release' ).tagVersion + ' -m "tagging" ', function ( err, stdout, stderr ) {
                if ( err ) {
                    grunt.fatal( 'Can not create the tag:\n  ' + stderr );
                }
                grunt.log.ok( 'Tagged' );
                done(err);
                grunt.task.run( ['bump'] );
            } );

        } );


    } );

    /**
     * This task does the following :
     * - Update the version in package.json to the next development version (0.11.0-dev) and commit.
     * - Calls 'checkout' task
     */
    grunt.registerTask( 'bump', "bump and commit", function () {
        var opts = this.options( {
            files: ['package.json'],
            commitFiles: ['package.json']
        } );
        var done = grunt.task.current.async();
        var VERSION_REGEXP = /([\'|\"]version[\'|\"][ ]*:[ ]*[\'|\"])([\d||A-a|.|-]*)([\'|\"])/i;

        opts.files.forEach( function ( file, idx ) {
            var version = null;
            var content = grunt.file.read( file ).replace( VERSION_REGEXP, function ( match, prefix, parsedVersion, suffix ) {
                grunt.log.ok( semver.valid( parsedVersion ) );
                if ( grunt.config( 'release.bumpType' ) === 'custom' ) {
                    version = grunt.config( 'release' ).nextVersion
                } else {
                    version = grunt.config( 'release' ).bumpType
                }
                return prefix + version + suffix;
            } );

            if ( !version ) {
                grunt.fatal( 'Can not find a version to bump in ' + file );
            }

            grunt.file.write( file, content );
            grunt.log.ok( 'Version bumped to ' + version + (opts.files.length > 1 ? ' (in ' + file + ')' : '') );
            var bumpCommit = 'git commit ' + opts.commitFiles.join( ' ' ) + ' -m "bump to next version"';
            grunt.log.ok( bumpCommit );

            exec( 'git commit ' + opts.commitFiles.join( ' ' ) + ' -m "bump to next version"', function ( err, stdout, stderr ) {
                if ( err ) {
                    grunt.fatal( 'Can not create the commit:\n  ' + stderr );
                }
                grunt.log.ok( 'Commit bump release' );
                done(err);
                grunt.task.run( ['checkout'] );
            } );
        } );
    } );

    /**
     * This task does the following :
     * - cd to the UPS local repository, defined in the release.ups_repo variable.
     * Note : please a repo with a clean state
     * - Checkout the master branch.
     * - Delete any existing 'ui_update' branch
     * - It created a new branch called ui_update
     * - Calls 'copy:server_dist' task.
     */
    grunt.registerTask( 'checkout', "create branch on UPS", function () {
        var done = grunt.task.current.async();
        exec( 'cd ' + grunt.config("local.ups_repo") + ';' + 'git checkout master;git branch -D ui_update;git checkout -b ui_update', function ( err, stdout, stderr ) {
            if ( err ) {
                grunt.fatal( 'Can not create the branch:\n  ' + stderr );
            }
            grunt.log.ok( 'Create ui_update branch' );
            done(err);
            grunt.task.run( ['copy:server_dist'] );
        } );
    });

    /**
     * This task does the following :
     * - cd to the UPS local repository, defined in the release.ups_repo variable.
     * - Stage changed files for commit.
     * - Commit.
     */
    grunt.registerTask( 'commitBranch',"commit UI update to UPS branch", function(){
        var done = grunt.task.current.async();
        exec( 'cd ' + grunt.config("local.ups_repo") + ';' + 'git add .;git commit . -m "UI update"', function ( err, stdout, stderr ) {
            if ( err ) {
                grunt.fatal( 'Can not commit the UI updates :\n  ' + stderr );
            }
            grunt.log.ok( 'Create ui update commit' );
            done(err);
        } );
    });
};