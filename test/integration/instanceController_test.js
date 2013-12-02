module('App.InstanceIndexController', {
    setup: function() {
        var apps = [
            {
                "name": "Cool App 1",
                "description": "A Cool App for testing",
                "pushApplicationID": "12345",
                "masterSecret": "3ababa8f-cc35-455b-8fc1-311ffe206538",
                "androidVariants": [
                    {
                        "name": "Android Version",
                        "variantID": "12345",
                        "secret": "123456",
                        "description": "An Android Variant",
                        "type": "ANDROID",
                        "installations": [
                            {
                                "id": "1",
                                "alias": "Android Installation",
                                "enabled": true,
                                "deviceToken": "1234567",
                                "platform": "android"
                            }
                        ],
                    }
                ],
                "simplePushVariants": [
                    {
                        "name": "Simple Push Version",
                        "variantID": "12345",
                        "secret": "123456",
                        "description": "A SimplePush Variant",
                        "type": "SIMPLE_PUSH",
                        "installations": [
                            {
                                "id": "2",
                                "alias": "Simple Push Installation",
                                "enabled": true,
                                "deviceToken": "1234567",
                                "simplePushEndpoint": "http://endpoint",
                                "platform": "simplePush"
                            }
                        ],
                    }
                ],
                "iosvariants": [
                    {
                        "name": "iOS Version",
                        "variantID": "12345",
                        "secret": "123456",
                        "description": "An iOS Variant",
                        "type": "IOS",
                        "installations": [
                            {
                                "id": "3",
                                "alias": "iOS Installation",
                                "enabled": true,
                                "deviceToken": "1234567",
                                "platform": "ios"
                            }
                        ],
                    }
                ],
                "chromePackagedAppVariants": [
                    {
                        "name": "Chrome Packaged App Version",
                        "variantID": "12345",
                        "secret": "123456",
                        "description": "A Chrome Packaged App Variant",
                        "type": "CHROME",
                        "installations": [
                            {
                                "id": "4",
                                "alias": "Chrome Packaged App Installation",
                                "enabled": true,
                                "deviceToken": "1234567",
                                "platform": "chromePackagedApp"
                            }
                        ],
                    }
                ]
            }
        ];

        $.mockjax({
            url: App.baseURL + "rest/applications",
            type: "GET",
            dataType: 'json',
            response: function( arguments ) {
                this.responseText = apps;
            }
        });

        $.mockjax({
            url: App.baseURL + "rest/applications/12345/installations/1",
            type: "GET",
            dataType: 'json',
            response: function( arguments ) {
                this.responseText = apps[ 0 ].androidVariants[ 0 ].installations[ 0 ];
            }
        });

        $.mockjax({
            url: App.baseURL + "rest/applications/12345/installations/1",
            type: "PUT",
            dataType: 'json',
            response: function( arguments ) {
                var data = JSON.parse( arguments.data );
                apps[ 0 ].androidVariants[ 0 ].installations[ 0 ].enabled = data.enabled;
                this.responseText = apps[ 0 ].androidVariants[ 0 ].installations[ 0 ];
            }
        });

        $.mockjax({
            url: App.baseURL + "rest/applications/12345/installations/2",
            type: "GET",
            dataType: 'json',
            response: function( arguments ) {
                this.responseText = apps[ 0 ].simplePushVariants[ 0 ].installations[ 0 ];
            }
        });

        $.mockjax({
            url: App.baseURL + "rest/applications/12345/installations/2",
            type: "PUT",
            dataType: 'json',
            response: function( arguments ) {
                var data = JSON.parse( arguments.data );
                apps[ 0 ].simplePushVariants[ 0 ].installations[ 0 ].enabled = data.enabled;
                this.responseText = apps[ 0 ].simplePushVariants[ 0 ].installations[ 0 ];
            }
        });

        $.mockjax({
            url: App.baseURL + "rest/applications/12345/installations/3",
            type: "GET",
            dataType: 'json',
            response: function( arguments ) {
                this.responseText = apps[ 0 ].iosvariants[ 0 ].installations[ 0 ];
            }
        });

        $.mockjax({
            url: App.baseURL + "rest/applications/12345/installations/3",
            type: "PUT",
            dataType: 'json',
            response: function( arguments ) {
                var data = JSON.parse( arguments.data );
                apps[ 0 ].iosvariants[ 0 ].installations[ 0 ].enabled = data.enabled;
                this.responseText = apps[ 0 ].iosvariants[ 0 ].installations[ 0 ];
            }
        });

        $.mockjax({
            url: App.baseURL + "rest/applications/12345/installations/4",
            type: "GET",
            dataType: 'json',
            response: function( arguments ) {
                this.responseText = apps[ 0 ].chromePackagedAppVariants[ 0 ].installations[ 0 ];
            }
        });

        $.mockjax({
            url: App.baseURL + "rest/applications/12345/installations/4",
            type: "PUT",
            dataType: 'json',
            response: function( arguments ) {
                var data = JSON.parse( arguments.data );
                apps[ 0 ].chromePackagedAppVariants[ 0 ].installations[ 0 ].enabled = data.enabled;
                this.responseText = apps[ 0 ].chromePackagedAppVariants[ 0 ].installations[ 0 ];
            }
        });

        App.reset();
        var controller = App.__container__.lookup("controller:instanceIndex");
        this.controller = controller;
    },
    teardown: function() {
        $.mockjaxClear();
    }
});

test( "visit android variant page", function() {
    visit( "/mobileApps/instances/12345/android/12345/1" ).then( function() {
        equal( exists( "table tbody tr td:eq(0):contains('Android Installation')" ), true, "Should be on the Installation page, but not" );
    });
});

test( "Toggle Android installation", function() {
    visit( "/mobileApps/instances/12345/android/12345/1" )
    .then( function() {
        equal( exists( "table tbody tr td:eq(4):contains('Enabled')" ), true, "Installation should be enabled, but is not" );
    })
    .click( "table tbody tr td:eq(5) a" )
    .then( function() {
        equal( exists( "table tbody tr td:eq(4):contains('Disabled')" ), true, "Installation should be disabled, but is not" );
    })
});

test( "visit ios variant page", function() {
    visit( "/mobileApps/instances/12345/ios/12345/3" ).then( function() {
        equal( exists( "table tbody tr td:eq(0):contains('iOS Installation')" ), true, "Should be on the Installation page, but not" );
    });
});

test( "Toggle iOS installation", function() {
    visit( "/mobileApps/instances/12345/android/12345/3" )
    .then( function() {
        equal( exists( "table tbody tr td:eq(4):contains('Enabled')" ), true, "Installation should be enabled, but is not" );
    })
    .click( "table tbody tr td:eq(5) a" )
    .then( function() {
        equal( exists( "table tbody tr td:eq(4):contains('Disabled')" ), true, "Installation should be disabled, but is not" );
    })
});

test( "visit chrome packaged app variant page", function() {
    visit( "/mobileApps/instances/12345/chrome/12345/4" ).then( function() {
        equal( exists( "table tbody tr td:eq(0):contains('Chrome Packaged App Installation')" ), true, "Should be on the Installation page, but not" );
    });
});

test( "Toggle chrome packaged app installation", function() {
    visit( "/mobileApps/instances/12345/android/12345/4" )
    .then( function() {
        equal( exists( "table tbody tr td:eq(4):contains('Enabled')" ), true, "Installation should be enabled, but is not" );
    })
    .click( "table tbody tr td:eq(5) a" )
    .then( function() {
        equal( exists( "table tbody tr td:eq(4):contains('Disabled')" ), true, "Installation should be disabled, but is not" );
    })
});

test( "visit simple push variant page", function() {
    visit( "/mobileApps/instances/12345/simplePush/12345/2" ).then( function() {
        equal( exists( "table tbody tr td:eq(0):contains('Simple Push Installation')"), true, "Should be on the Installation page, but not" );
    });
});

test( "Toggle simplePush installation", function() {
    visit( "/mobileApps/instances/12345/android/12345/2" )
    .then( function() {
        equal( exists( "table tbody tr td:eq(4):contains('Enabled')" ), true, "Installation should be enabled, but is not" );
    })
    .click( "table tbody tr td:eq(5) a" )
    .then( function() {
        equal( exists( "table tbody tr td:eq(4):contains('Disabled')" ), true, "Installation should be disabled, but is not" );
    })
});
