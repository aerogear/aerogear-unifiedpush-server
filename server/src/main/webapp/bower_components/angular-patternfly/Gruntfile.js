'use strict';

module.exports = function(grunt) {

  require('matchdep').filterDev('grunt-*').forEach(grunt.loadNpmTasks);

  function init() {

    grunt.initConfig({
      availabletasks: {
        tasks: {
          options: {
            descriptions: {
              'help' : 'Task list helper for your Grunt enabled projects.',
              'clean' : 'Deletes the content of the dist directory.',
              'build' : 'Builds the project (including documentation) into the dist directory. You can specify modules to be built as arguments (' +
                'grunt build:buttons:notification) otherwise all available modules are built.',
              'test' : 'Executes the karma testsuite.',
              'watch' : 'Whenever js source files (from the src directory) change, the tasks executes jshint and documentation build.',
              'ngdocs' : 'Builds documentation into dist/docs.',
              'ngdocs:view' : 'Builds documentation into dist/docs and runs a web server. The docs can be accessed on http://localhost:8000/'
            },
            groups: {
              'Basic project tasks': ['help', 'clean', 'build', 'test'],
              'Documentation tasks': ['ngdocs', 'ngdocs:view']
            }
          }
        }
      },
      clean: {
        docs: ['dist/docs'],
        all: ['dist/*']
      },
      concat: {
        options: {
          separator: ';'
        },
        dist: {
          src: 'src/*/*.js',
          dest: 'dist/angular-patternfly.js'
        }
      },
      connect: {
        docs: {
          options: {
            base: 'dist/docs'
          }
        }
      },
      jshint: {
        files: ['Gruntfile.js', 'src/*/*.js'],
        options: {
          jshintrc: '.jshintrc'
        },
        beforeconcat: {
          options: {
            force: true,
            ignores: ['**.min.js']
          },
          files: {
            src: 'src/*/*.js'
          }
        }
      },
      karma: {
        unit: {
          configFile: 'karma.conf.js',
          singleRun: true,
          browsers: ['PhantomJS']
        }
      },
      ngdocs: {
        options: {
          dest: 'dist/docs',
          scripts: ['angular.js', 'dist/angular-patternfly.min.js'],
          html5Mode: false
        },
        all: ['src/*/*.js']
      },
      uglify: {
        options: {
          mangle: false
        },
        build: {
          files: {},
          src: 'dist/angular-patternfly.js',
          dest: 'dist/angular-patternfly.min.js'
        }
      },
      watch: {
        main: {
          files: ['Gruntfile.js'],
          tasks: ['jshint']
        },
        js: {
          files: ['Gruntfile.js', 'src/*/*.js'],
          tasks: ['build']
        }
      }
    });

    // You can specify which modules to build as arguments of the build task.
    grunt.registerTask('build', 'Create bootstrap build files', function() {
      var concatSrc = [];

      if (this.args.length) {
        this.args.forEach(function(file){
          if (grunt.file.exists('./src/'+file)){
            grunt.log.ok('Adding ' + file + ' to the build queue.');
            concatSrc.push('src/'+file+'/*.js');
          } else {
            grunt.fail.warn('Unable to build module \'' + file + '\'. The module doesn\'t exist.');
          }
        });

      } else {
        concatSrc = 'src/*/*.js';
      }

      grunt.task.run(['clean', 'jshint:beforeconcat', 'jshint', 'test', 'concat', 'uglify:build', 'ngdocs']);
    });

    grunt.registerTask('default', ['build']);
    grunt.registerTask('ngdocs:view', ['build', 'connect:docs', 'watch']);
    grunt.registerTask('test', ['karma']);
    grunt.registerTask('help', ['availabletasks']);

  }

  init({});

};