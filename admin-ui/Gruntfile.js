// Generated on 2014-03-25 using generator-angular 0.4.0
'use strict';
var LIVERELOAD_PORT = 35729;

// # Globbing
// for performance reasons we're only matching one level down:
// 'test/spec/{,*/}*.js'
// use this if you want to recursively match all subfolders:
// 'test/spec/**/*.js'

module.exports = function (grunt) {
  require('matchdep').filterDev('grunt-*').forEach(grunt.loadNpmTasks);

  // load custom tasks
  grunt.loadTasks('tasks');

  // configurable paths
  var yeomanConfig = {
    lib: 'app/bower_components',
    app: 'app',
    dist: 'dist',
    tmp: '.tmp',
    webappDist: '../server/src/main/webapp'
  };

  try {
    yeomanConfig.app = require('./bower.json').appPath || yeomanConfig.app;
  } catch (e) {
  }

  grunt.initConfig({
    yeoman: yeomanConfig,
    less: {
      main: {
        options: {
          paths: ['<%= yeoman.lib %>/patternfly/less', '<%= yeoman.lib %>']
        },
        src: '<%= yeoman.app %>/styles/main.less',
        dest: '<%= yeoman.tmp %>/styles/compiled-less.css'
      }
    },
    watch: {
      options: {
        nospawn: true
      },
      less: {
        files: '<%= yeoman.app %>/styles/*.less',
        tasks: ['less', 'newer:copy:webapp', 'newer:copy:jbossweb']
      },
      livereload: {
        options: {
          livereload: LIVERELOAD_PORT
        },
        files: [
          '<%= yeoman.app %>/**/*.html',
          '{<%= yeoman.app %>,<%= yeoman.tmp %>}/styles/{,*/}*.css',
          '{<%= yeoman.app %>,<%= yeoman.tmp %>}/scripts/{,*/}*.js',
          '<%= yeoman.app %>/images/{,*/}*.{png,jpg,jpeg,gif,webp,svg}'
        ],
        tasks: [ 'newer:copy:webapp', 'newer:copy:jbossweb' ]
      }
    },
    autoprefixer: {
      options: ['last 1 version'],
      dist: {
        files: [
          {
            expand: true,
            cwd: '.tmp/styles/',
            src: '{,*/}*.css',
            dest: '.tmp/styles/'
          }
        ]
      }
    },
    clean: {
      dist: {
        files: [
          {
            dot: true,
            src: [
              '.tmp',
              '<%= yeoman.dist %>/*',
              '!<%= yeoman.dist %>/.git*'
            ]
          }
        ]
      },
      webappDist: {
        options: {
          'force': true
        },
        files: [
          {
            src: [
              '<%= yeoman.webappDist %>/*',
              '!<%= yeoman.webappDist %>/WEB-INF'
            ]
          }
        ]
      },
      jbosswebDist: {
        options: {
          'force': true
        },
        files: [
          {
            src: [
              '<%= local.jbossweb %>/*',
              '!<%= local.jbossweb %>/WEB-INF',
              '!<%= local.jbossweb %>/META-INF'
            ]
          }
        ]
      },
      server: '.tmp'
    },
    jshint: {
      options: {
        jshintrc: '.jshintrc'
      },
      all: [
        'Gruntfile.js',
        '<%= yeoman.app %>/scripts/{,*/}*.js'
      ]
    },
    // not used since Uglify task does concat,
    // but still available if needed
    /*concat: {
     dist: {}
     },*/
    rev: {
      dist: {
        files: {
          src: [
            '<%= yeoman.dist %>/scripts/{,*/}*.js',
            '<%= yeoman.dist %>/styles/{,*/}*.css',
            '<%= yeoman.dist %>/img/{,*/}*.{png,jpg,jpeg,gif,webp,svg}'
          ]
        }
      }
    },
    useminPrepare: {
      html: [
        '<%= yeoman.app %>/index.html',
        '<%= yeoman.app %>/directives/{,*/}*.html',
        '<%= yeoman.app %>/views/{,*/}*.html'
      ],
      options: {
        dest: '<%= yeoman.dist %>'
      }
    },
    usemin: {
      html: ['<%= yeoman.dist %>/**/*.html'],
      css: ['<%= yeoman.dist %>/styles/{,*/}*.css'],
      options: {
        dirs: ['<%= yeoman.dist %>']
      }
    },
    imagemin: {
      dist: {
        files: [
          {
            expand: true,
            cwd: '<%= yeoman.app %>/img',
            src: '{,*/}*.{png,jpg,jpeg}',
            dest: '<%= yeoman.dist %>/img'
          }
        ]
      }
    },
    svgmin: {
      dist: {
        files: [
          {
            expand: true,
            cwd: '<%= yeoman.app %>/images',
            src: '{,*/}*.svg',
            dest: '<%= yeoman.dist %>/images'
          }
        ]
      }
    },
    htmlmin: {
      dist: {
        options: {
          /*removeCommentsFromCDATA: true,
           // https://github.com/yeoman/grunt-usemin/issues/44
           //collapseWhitespace: true,
           collapseBooleanAttributes: true,
           removeAttributeQuotes: true,
           removeRedundantAttributes: true,
           useShortDoctype: true,
           removeEmptyAttributes: true,
           removeOptionalTags: true*/
        },
        files: [
          {
            expand: true,
            cwd: '<%= yeoman.app %>',
            src: ['*.html', 'views/*.html'],
            dest: '<%= yeoman.dist %>'
          }
        ]
      }
    },
    // Put files not handled in other tasks here
    copy: {
      // we need to put patternfly fonts to the correct destination
      // ( https://github.com/patternfly/patternfly/issues/20 )
      fonts: {
        files: [
          {
            expand: true,
            dot: true,
            cwd: '<%= yeoman.lib %>/font-awesome/fonts/',
            dest: '<%= yeoman.tmp %>/fonts/',
            src: [ '**' ]
          },
          {
            expand: true,
            dot: true,
            cwd: '<%= yeoman.lib %>/patternfly/dist/fonts/',
            dest: '<%= yeoman.tmp %>/fonts/',
            src: [ '**' ]
          },
          {
            expand: true,
            dot: true,
            cwd: '<%= yeoman.app %>/styles/fonts/exo2/',
            dest: '<%= yeoman.tmp %>/fonts/',
            src: [ '**', '!*.less', '!*.txt' ]
          }
        ]
      },
      dist: {
        files: [
          {
            expand: true,
            dot: true,
            cwd: '<%= yeoman.app %>',
            dest: '<%= yeoman.dist %>',
            src: [
              '*.{ico,txt}',
              '.htaccess',
              'img/{,*/}*.{webp,gif,png,svg}',
              'directives/**',
              'views/**'
            ]
          },
          {
            expand: true,
            cwd: '<%= yeoman.tmp %>',
            dest: '<%= yeoman.dist %>',
            src: [
              '**',
              '!styles/compiled-less.css'
            ]
          }
        ]
      },
      webapp: {
        files: [
          {
            expand: true,
            cwd: '<%= yeoman.tmp %>',
            dest: '<%= local.webapp %>',
            src: [ '**' ]
          },
          {
            expand: true,
            cwd: '<%= yeoman.app %>',
            dest: '<%= local.webapp %>',
            src: [ '**', '!**/*.txt', '!**/*.less' ]
          }
        ]
      },
      jbossweb: {
        files: [
          {
            expand: true,
            cwd: '<%= yeoman.tmp %>',
            dest: '<%= local.jbossweb %>',
            src: [ '**', '!**/*.txt', '!**/*.less' ]
          },
          {
            expand: true,
            cwd: '<%= yeoman.app %>',
            dest: '<%= local.jbossweb %>',
            src: [ '**', '!**/*.txt' ]
          }
        ]
      },
      webappDist: {
        files: [
          {
            expand: true,
            cwd: '<%= yeoman.dist %>',
            dest: '<%= yeoman.webappDist %>',
            src: [ '**', '!**/*.txt' ]
          }
        ]
      },
      jbosswebDist: {
        files: [
          {
            expand: true,
            cwd: '<%= yeoman.dist %>',
            dest: '<%= local.jbossweb %>',
            src: [ '**', '!**/*.txt' ]
          }
        ]
      }
    },
    concurrent: {
      server: [

      ],
      test: [

      ],
      dist: [
        'copy:styles',
        'imagemin',
        'htmlmin'
      ]
    },
    karma: {
      unit: {
        configFile: 'karma.conf.js',
        singleRun: true
      }
    },
    cdnify: {
      dist: {
        html: ['<%= yeoman.dist %>/*.html']
      }
    },
    ngmin: {
      dist: {
        files: [
          {
            expand: true,
            cwd: '<%= yeoman.dist %>/scripts',
            src: '*.js',
            dest: '<%= yeoman.dist %>/scripts'
          }
        ]
      }
    }
  });

  grunt.loadNpmTasks('assemble-less');

  grunt.registerTask('server', function (target) {
    if (target === 'dist') {
      return grunt.task.run(['build']);
    }

    grunt.task.run([
      'initLocalConfig',
      'clean:server',
      'concurrent:server',
      'less',
      'copy:fonts',
      'copy:webapp',
      'copy:jbossweb',
      'autoprefixer',
      'watch'
    ]);
  });

  grunt.registerTask('test', [
    'clean:server',
    'concurrent:test',
    'autoprefixer'
  ]);

  grunt.registerTask('build', [
    'clean:dist',
    'less',
    'copy:fonts',
    'useminPrepare',
    'imagemin',
    'htmlmin',
    'concat',
//    'ngmin:dist',
//    'uglify',
    'copy:dist',
    'rev',
    'usemin'
  ]);

  grunt.registerTask('default', [
    'initLocalConfig',
    'jshint',
    'test',
    'build'
  ]);

  grunt.registerTask('dist', [
    'initLocalConfig',
    'default',
    'clean:webappDist',
    'copy:webappDist'
  ]);

  grunt.registerTask('jbosswebDist', [
    'dist',
    'clean:jbosswebDist',
    'copy:jbosswebDist'
  ]);

  grunt.registerTask('copy_web', ['copy:webapp']);
  grunt.registerTask('jboss_web', ['copy:jbossweb']);
};
