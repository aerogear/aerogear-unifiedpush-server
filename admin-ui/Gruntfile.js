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
    tmp: '.tmp'
  };

  try {
    yeomanConfig.app = require('./bower.json').appPath || yeomanConfig.app;
  } catch (e) {
  }

  grunt.initConfig({
    yeoman: yeomanConfig,
    local: {
      // default values used when local-config.json is not loaded
      home: './',
      jbossweb: '../servers/ups-server/target/ag-push'
    },
    less: {
      main: {
        options: {
          paths: ['<%= yeoman.lib %>']
        },
        src: '<%= yeoman.app %>/styles/main.less',
        dest: '<%= yeoman.tmp %>/styles/main.css'
      },
      account: {
        options: {
          paths: ['<%= yeoman.lib %>']
        },
        src: '<%= yeoman.app %>/styles/account.less',
        dest: '<%= yeoman.tmp %>/styles/account.css'
      },
      login: {
        options: {
          paths: ['<%= yeoman.lib %>']
        },
        src: '<%= yeoman.app %>/styles/login.less',
        dest: '<%= yeoman.tmp %>/styles/login.css'
      }
    },
    watch: {
      options: {
        nospawn: true
      },
      less: {
        files: '<%= yeoman.app %>/styles/*.less',
        tasks: ['less', 'newer:copy:jbossweb']
      },
      livereload: {
        options: {
          livereload: LIVERELOAD_PORT
        },
        files: [
          '<%= yeoman.app %>/**/*.html',
          '{<%= yeoman.app %>,<%= yeoman.tmp %>}/snippets/**',
          '{<%= yeoman.app %>,<%= yeoman.tmp %>}/components/{,*/}*.{html,js}',
          '{<%= yeoman.app %>,<%= yeoman.tmp %>}/components/**/*.{html,js}',
          '{<%= yeoman.app %>,<%= yeoman.tmp %>}/styles/{,*/}*.css',
          '{<%= yeoman.app %>,<%= yeoman.tmp %>}/scripts/{,*/}*.js',
          '<%= yeoman.app %>/images/{,*/}*.{png,jpg,jpeg,gif,webp,svg}'
        ],
        tasks: [ 'newer:copy:jbossweb' ]
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
      jbosswebDist: {
        options: {
          'force': true
        },
        src: [
          '<%= local.jbossweb %>/*',
          '!<%= local.jbossweb %>/config',
          '!<%= local.jbossweb %>/WEB-INF',
          '!<%= local.jbossweb %>/META-INF'
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
    filerev: {
      dist: {
        src: [
          '<%= yeoman.dist %>/scripts/{,*/}*.js',
          '<%= yeoman.dist %>/styles/{,*/}*.css',
          '<%= yeoman.dist %>/img/{,*/}*.{png,jpg,jpeg,gif,webp,svg}'
        ]
      }
    },
    useminPrepare: {
      html: [
        '<%= yeoman.app %>/index.html'
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
            src: [
              '*.html',
              'dialogs/*.html',
              'directives/*.html'
            ],
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
      misc: {
        files: [
          {
            expand: true,
            dot: true,
            cwd: '<%= yeoman.lib %>/zeroclipboard/dist/',
            dest: '<%= yeoman.tmp %>/img/',
            src: [ 'ZeroClipboard.swf' ]
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
              'components/**',
              'snippets/**',
              '*.{ico,txt}',
              '*.json',
              'img/{,*/}*.{webp,gif,png,jpg,svg}'
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
      nofilerev: {
        files: [
          {
            expand: true,
            dot: true,
            cwd: '<%= yeoman.dist %>/styles',
            dest: '<%= yeoman.dist %>/styles/',
            src: [
              'account.*.css',
              'login.*.css'
            ],
            rename: function(dest, src) {
              var renamed = dest + src.replace(/\..+\.css$/,'.css');
              console.log(renamed);
              return renamed;
            }
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
    },
    uglify: {
      options: {
        mangle: false,
        compress: true,
        report: true
      }
    },
    cssmin: {
      options: {
        report: 'min'
      }
    },
    ngtemplates:  {
      upsConsole: {
        src: [
          'components/**/**.html',
          'snippets/**/**.*',
          'directives/**.html',
          'dialogs/**.html'
        ],
        cwd: '<%= yeoman.app %>',
        dest: '<%= yeoman.tmp %>/ngtemplates/templates.js',
        options:    {
          usemin: 'scripts/templates.js'
        }
      }
    },
    bower: {
      install: {
        options: {
          targetDir: 'app/bower-components/'
        }
      }
    },
    compress: {
      main: {
        options: {
          archive: 'dist/npm-ups-admin-ui.tgz',
          mode: 'tgz'
        },
        files: [{
          src: [
            '.tmp/concat/scripts/modules.js',
            '.tmp/concat/scripts/scripts.js',
            '.tmp/ngtemplates/templates.js',
            'app/docs-links.json'
          ],
          dest: 'ups-admin-ui/public',
          filter: 'isFile',
          flatten: true,
          expand: true
        }, {
          src: ['package.json'],
          dest: 'ups-admin-ui/',
          filter: 'isFile',
          flatten: true,
          expand: true
        }]
      }
    }
  });

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
      'copy:misc',
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
    'copy:misc',
    'useminPrepare',
    'ngtemplates',
    'htmlmin',
    'concat',
    'cssmin',
    'ngmin:dist',
    'uglify',
    'copy:dist',
    'filerev',
    'copy:nofilerev',
    'usemin',
    'compress'
  ]);

  grunt.registerTask('default', [
    'jshint',
    'test',
    'build'
  ]);

  grunt.registerTask('dist', [
    'bower:install',
    'default'
  ]);

  grunt.registerTask('jbosswebDist', [
    'initLocalConfig',
    'dist',
    'clean:jbosswebDist',
    'copy:jbosswebDist'
  ]);

  grunt.registerTask('jboss_web', ['copy:jbossweb']);
};
