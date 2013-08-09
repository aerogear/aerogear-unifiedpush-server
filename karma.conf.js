// Karma configuration
// Generated on Thu Aug 08 2013 11:17:20 GMT-0400 (EDT)

module.exports = function(config) {
    config.set({

        // base path, that will be used to resolve files and exclude
        basePath: '.',


        // frameworks to use
        frameworks: ['qunit'],
        plugins: [
            'karma-qunit',
            'karma-chrome-launcher',
            'karma-ember-preprocessor',
            'karma-phantomjs-launcher'
        ],

        preprocessors: {
            "**/*.hbs": 'ember'
        },


        // list of files / patterns to load in the browser
        files: [
            "app/bower_components/jquery/jquery.min.js",
            "app/bower_components/handlebars/handlebars.js",
            "app/bower_components/ember/ember.js",
            "app/bower_components/aerogear/aerogear.min.js",
            "app/bower_components/jquery-mockjax/jquery.mockjax.js",
            "app/scripts/*.js",
            "app/scripts/vendor/ember-validations.min.js",
            "app/scripts/helpers/*.js",
            "app/scripts/controllers/*.js",
            "app/scripts/models/*.js",
            "app/scripts/views/*.js",
            "app/templates/**/*.hbs",
            "test/**/*.js",
            "app/styles/normalize.css",
            "app/styles/topcoat-desktop-light.min.css",
            "app/styles/main.css"
        ],


        // list of files to exclude
        exclude: [
            "app/scripts/vendor/ember-1.0.0-rc.6.1.js",
            "app/scripts/templates.js"
        ],


        // test results reporter to use
        // possible values: 'dots', 'progress', 'junit', 'growl', 'coverage'
        reporters: ['progress'],


        // web server port
        port: 9876,


        // enable / disable colors in the output (reporters and logs)
        colors: true,


        // level of logging
        // possible values: config.LOG_DISABLE || config.LOG_ERROR || config.LOG_WARN || config.LOG_INFO || config.LOG_DEBUG
        logLevel: config.LOG_INFO,


        // enable / disable watching file and executing tests whenever any file changes
        autoWatch: false,


        // Start these browsers, currently available:
        // - Chrome
        // - ChromeCanary
        // - Firefox
        // - Opera
        // - Safari (only Mac)
        // - PhantomJS
        // - IE (only Windows)
        browsers: ['PhantomJS'],


        // If browser does not capture in given timeout [ms], kill it
        captureTimeout: 60000,


        // Continuous Integration mode
        // if true, it capture browsers, run tests and exit
        singleRun: true
    });
};
