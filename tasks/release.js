'use strict';
var exec = require('child_process').exec;
var semver = require('semver');

module.exports = function ( grunt ) {

    grunt.registerTask( 'initLocalConfig',function(){
        if(!grunt.file.exists('./release-config.json')){
            var sampleContent = {
                home: '/home/sebastien/aerogear/aerogear-unified-push-server-admin-ui',
                webapp: "/home/sebastien/aerogear/aerogear-unifiedpush-server/src/main/webapp",
                jbossweb: "/home/sebastien/apps/jboss-as-7.1.1.Final/standalone/deployments/ag-push.war",
                ups_repo:"/home/sebastien/aerogear/ui_update/aerogear-unifiedpush-server"
            }
            grunt.file.write('./release-config.json',JSON.stringify(sampleContent,null,'\t'));
        }
        var config = grunt.config.getRaw();
        config.release = grunt.file.readJSON('./release-config.json');
    });

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
    grunt.registerTask( 'bump', "bump and commit", function () {
        var opts = this.options( {
            files: ['package.json'],
            commitFiles: ['package.json'], // '-a' for all files
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
                grunt.log.ok('<%= yeoman.app %>');
                done(err);
                grunt.task.run( ['checkout'] );
            } );
        } );
    } );

    grunt.registerTask( 'checkout', "create branch on UPS", function () {
        var done = grunt.task.current.async();
        exec( 'cd ' + grunt.config("release.ups_repo") + ';' + 'git checkout master;git branch -D ui_update;git checkout -b ui_update', function ( err, stdout, stderr ) {
            if ( err ) {
                grunt.fatal( 'Can not create the branch:\n  ' + stderr );
            }
            grunt.log.ok( 'Create ui_update branch' );
            done(err);
            grunt.task.run( ['copy:server_dist'] );
        } );
    });

    grunt.registerTask( 'commitBranch',"commit UI update to UPS branch", function(){
        var done = grunt.task.current.async();
        exec( 'cd ' + grunt.config("release.ups_repo") + ';' + 'git add .;git commit . -m "UI update"', function ( err, stdout, stderr ) {
            if ( err ) {
                grunt.fatal( 'Can not commit the UI updates :\n  ' + stderr );
            }
            grunt.log.ok( 'Create ui update commit' );
            done(err);
        } );
    });
};