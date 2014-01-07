// Generated on 2013-06-20 using generator-webapp 0.2.2
'use strict';
var LIVERELOAD_PORT = 35729;
var lrSnippet = require('connect-livereload')({port: LIVERELOAD_PORT});
var mountFolder = function (connect, dir) {
    return connect.static(require('path').resolve(dir));
};
var currentVersion = require('./package.json').version;
var semver = require('semver');

// # Globbing
// for performance reasons we're only matching one level down:
// 'test/spec/{,*/}*.js'
// use this if you want to recursively match all subfolders:
// 'test/spec/**/*.js'

module.exports = function (grunt) {
    // load all grunt tasks
    require('matchdep').filterDev('grunt-*').forEach(grunt.loadNpmTasks);

    // load custom tasks
    grunt.loadTasks('tasks');

    // configurable paths
    var yeomanConfig = {
        app: 'app',
        dist: 'dist'
    };

    grunt.initConfig({
        yeoman: yeomanConfig,
        emberTemplates: {
            compile: {
                options: {
                    templateName: function(sourceFile) {
                        return sourceFile.replace(/app\/templates\//, '');
                    }
                },
                files: {
                    "<%= yeoman.app %>/scripts/templates.js": ["<%= yeoman.app %>/templates/**/*.{handlebars,hbs}"]
                }
            }
        },
        watch: {
            options: {
                nospawn: true
            },
            livereload: {
                options: {
                    livereload: LIVERELOAD_PORT
                },
                files: [
                    '<%= yeoman.app %>/*.html',
                    '<%= yeoman.app %>/templates/{,*/}*.{handlebars,hbs}',
                    '{.tmp,<%= yeoman.app %>}/styles/{,*/}*.css',
                    '{.tmp,<%= yeoman.app %>}/scripts/{,*/}*.js',
                    '<%= yeoman.app %>/images/{,*/}*.{png,jpg,jpeg,gif,webp,svg}'
                ],
                tasks: [ 'emberTemplates', 'copy:webapp', 'copy:jbossweb' ]
            }
        },
        connect: {
            options: {
                port: 9000,
                // change this to '0.0.0.0' to access the server from outside
                hostname: '0.0.0.0'
            },
            livereload: {
                options: {
                    middleware: function (connect) {
                        return [
                            mountFolder(connect, '.tmp'),
                            mountFolder(connect, yeomanConfig.app),
                            lrSnippet
                        ];
                    }
                }
            },
            test: {
                options: {
                    middleware: function (connect) {
                        return [
                            mountFolder(connect, '.tmp'),
                            mountFolder(connect, 'test')
                        ];
                    }
                }
            },
            dist: {
                options: {
                    middleware: function (connect) {
                        return [
                            mountFolder(connect, yeomanConfig.dist)
                        ];
                    }
                }
            }
        },
        open: {
            server: {
                path: 'http://localhost:<%= connect.options.port %>'
            }
        },
        clean: {
            dist: {
                files: [{
                    dot: true,
                    src: [
                        '.tmp',
                        '<%= yeoman.dist %>/*',
                        '!<%= yeoman.dist %>/.git*'
                    ]
                }]
            },
            server: '.tmp'
        },
        jshint: {
            options: {
                jshintrc: '.jshintrc'
            },
            all: [
                'Gruntfile.js',
                '<%= yeoman.app %>/scripts/{,*/}*.js',
                '!<%= yeoman.app %>/scripts/vendor/*',
                '!<%= yeoman.app %>/scripts/templates.js',
                'test/spec/{,*/}*.js'
            ]
        },
        qunit: {
            files: ['test/unit/index.html','test/integration/index.html'] //'test/integration/index.html' add this back in once i figure out how the run loop can work with tests
        },
        // not used since Uglify task does concat,
        // but still available if needed
        /*concat: {
         dist: {}
         },*/
        // not enabled since usemin task does concat and uglify
        // check index.html to edit your build targets
        // enable this task if you prefer defining your build targets here
        /*uglify: {
         dist: {}
         },*/
        rev: {
            dist: {
                files: {
                    src: [
                        '<%= yeoman.dist %>/scripts/{,*/}*.js',
                        '<%= yeoman.dist %>/styles/{,*/}*.css',
                        '<%= yeoman.dist %>/images/{,*/}*.{png,jpg,jpeg,gif,webp}',
                        '<%= yeoman.dist %>/styles/fonts/*'
                    ]
                }
            }
        },
        useminPrepare: {
            options: {
                dest: '<%= yeoman.dist %>'
            },
            html: '<%= yeoman.app %>/index.html'
        },
        usemin: {
            options: {
                dirs: ['<%= yeoman.dist %>']
            },
            html: ['<%= yeoman.dist %>/{,*/}*.html'],
            css: ['<%= yeoman.dist %>/styles/{,*/}*.css']
        },
        imagemin: {
            dist: {
                files: [{
                    expand: true,
                    cwd: '<%= yeoman.app %>/img',
                    src: '{,*/}*.{png,jpg,jpeg}',
                    dest: '<%= yeoman.dist %>/img'
                }]
            }
        },
        // svgmin: {
        //     dist: {
        //         files: [{
        //             expand: true,
        //             cwd: '<%= yeoman.app %>/img',
        //             src: '{,*/}*.svg',
        //             dest: '<%= yeoman.dist %>/img'
        //         }]
        //     }
        // },
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
                files: [{
                    expand: true,
                    cwd: '<%= yeoman.app %>',
                    src: '*.html',
                    dest: '<%= yeoman.dist %>'
                }]
            }
        },
        // Put files not handled in other tasks here
        copy: {
            dist: {
                files: [{
                    expand: true,
                    dot: true,
                    cwd: '<%= yeoman.app %>',
                    dest: '<%= yeoman.dist %>',
                    src: [
                        '*.{ico,txt}',
                        '.htaccess',
                        'img/{,*/}*.{webp,gif,png,svg}',
                        'font/*'
                    ]
                }, {
                    expand: true,
                    cwd: '.tmp/images',
                    dest: '<%= yeoman.dist %>/images',
                    src: [
                        'generated/*'
                    ]
                }]
            },
            webapp: {
                files: [{
                    expand: true,
                    cwd: '<%= yeoman.app %>',
                    dest: '<%= local.webapp %>',
                    src: [ "**", "!**/*.txt" ]
                }]
            },
            jbossweb: {
                files: [{
                    expand: true,
                    cwd: '<%= yeoman.app %>',
                    dest: '<%= local.jbossweb %>',
                    src: [ "**", "!**/*.txt" ]
                }]
            },
            server_dist: {
                files: [{
                    expand: true,
                    cwd: '<%= local.home %>/dist',
                    dest: '<%= local.ups_repo %>/src/main/webapp',
                    src: [ "**", "!**/*.txt" ]
                }]
            }
        },
        concurrent: {
            server: [ ],
            test: [ ],
            dist: [
                'imagemin',
                //'svgmin',
                'htmlmin',
                'emberTemplates'
            ]
        },
        prompt: {
            release: {
                options: {
                    questions: [
                        {
                            config: 'release.tagVersion',
                            type: 'input',
                            message: 'Tag version',
                            "default": currentVersion.slice(0,-4),
                            validate: function(value) {
                                if (value === '') {
                                    return 'A value is required.';
                                }
                                return true;
                            }
                        },
                        {
                            config: 'release.bumpType',
                            type: 'list',
                            message: 'Bump type',
                            choices: [
                                {
                                    value: semver.inc(currentVersion, 'patch') + '-dev',
                                    name: 'Patch:  '.yellow + semver.inc(currentVersion, 'patch').yellow + '-dev'.yellow
                                },
                                {
                                    value: semver.inc(currentVersion, 'minor') + '-dev',
                                    name: 'Minor:  '.yellow + semver.inc(currentVersion, 'minor').yellow + '-dev'.yellow
                                },
                                {
                                    value: semver.inc(currentVersion, 'major') + '-dev',
                                    name: 'Major:  '.yellow + semver.inc(currentVersion, 'major').yellow + '-dev'.yellow
                                },
                                {
                                    value: 'custom',
                                    name: 'Custom: x.x.x'.yellow +
                                        '   Specify version...'
                                }
                            ],
                            "default": 'patch'
                        },
                        {
                            config: 'release.nextVersion',
                            type: 'input',
                            message: 'Custom Version ?',
                            when: function (answers) {
                                return answers['release.bumpType'] === 'custom';
                            },
                            validate: function(value) {
                                if (value === '') {
                                    return 'A value is required.';
                                }
                                return true;
                            }
                        }
                    ]
                }
            }
        },

        shell: {
            branch: {
                command: [
                    'cd <%= local.ups_repo %>',
                    'git checkout -b ui_update',
                    'cp  <%= local.home %>/dist <%= local.ups_repo %>/src/main/webapp',
                    'git commit . -m "new Admin UI version"'
                ].join('&&')
            }
        }
    });

    grunt.registerTask('server', function (target) {
        if (target === 'dist') {
            return grunt.task.run(['build', 'open', 'connect:dist:keepalive']);
        }

        grunt.task.run([
            'initLocalConfig',
            'clean:server',
            'concurrent:server',
            'emberTemplates',
            'connect:livereload',
            'open',
            'watch'
        ]);
    });

    grunt.registerTask('test', [
        'clean:server',
        'concurrent:test',
        'connect:test',
        'emberTemplates',
        'qunit'
    ]);

    grunt.registerTask('build', [
        'clean:dist',
        'useminPrepare',
        'concurrent:dist',
        'concat',
        'cssmin',
        'uglify',
        'copy:dist',
        'rev',
        'usemin'
    ]);

    grunt.registerTask('default', [
        'jshint',
        'test',
        'build'
    ]);

    grunt.registerTask('copy_web', ['copy:webapp']);
    grunt.registerTask('jboss_web', ['copy:jbossweb']);
    grunt.registerTask('release', ['initLocalConfig','default','prompt:release','tag','commitBranch']);

};
