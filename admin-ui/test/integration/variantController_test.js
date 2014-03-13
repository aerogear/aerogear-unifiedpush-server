module('App.VariantIndexController', {
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
            url: App.baseURL + "rest/applications/12345/android/12345",
            type: "GET",
            dataType: 'json',
            response: function( arguments ) {
                this.responseText = apps[ 0 ].androidVariants[ 0 ];
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
            url: App.baseURL + "rest/applications/12345/installations/2",
            type: "GET",
            dataType: 'json',
            response: function( arguments ) {
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
            url: App.baseURL + "rest/applications/12345/installations/4",
            type: "GET",
            dataType: 'json',
            response: function( arguments ) {
                this.responseText = apps[ 0 ].chromePackagedAppVariants[ 0 ].installations[ 0 ];
            }
        });

        $.mockjax({
            url: App.baseURL + "rest/applications/12345/ios/12345",
            type: "GET",
            dataType: 'json',
            response: function( arguments ) {
                this.responseText = apps[ 0 ].iosvariants[ 0 ];
            }
        });

        $.mockjax({
            url: App.baseURL + "rest/applications/12345/chrome/12345",
            type: "GET",
            dataType: 'json',
            response: function( arguments ) {
                this.responseText = apps[ 0 ].chromePackagedAppVariants[ 0 ];
            }
        });

        $.mockjax({
            url: App.baseURL + "rest/applications/12345/simplePush/12345",
            type: "GET",
            dataType: 'json',
            response: function( arguments ) {
                this.responseText = apps[ 0 ].simplePushVariants[ 0 ];
            }
        });

        $.mockjax({
            url: App.baseURL + "rest/applications/12345/android/12345/reset",
            type: "PUT",
            dataType: 'json',
            response: function( arguments ) {

                apps[ 0 ].masterSecret = "0987654321";

                this.responseText = apps[ 0 ].androidVariants[ 0 ];
            }
        });

        $.mockjax({
            url: App.baseURL + "rest/applications/12345/iOS/12345/reset",
            type: "PUT",
            dataType: 'json',
            response: function( arguments ) {

                apps[ 0 ].masterSecret = "0987654321";

                this.responseText = apps[ 0 ].iosvariants[ 0 ];
            }
        });

        $.mockjax({
            url: App.baseURL + "rest/applications/12345/chrome/12345/reset",
            type: "PUT",
            dataType: 'json',
            response: function( arguments ) {

                apps[ 0 ].masterSecret = "0987654321";

                this.responseText = apps[ 0 ].chromePackagedAppVariants[ 0 ];
            }
        });

        $.mockjax({
            url: App.baseURL + "rest/applications/12345/simplePush/12345/reset",
            type: "PUT",
            dataType: 'json',
            response: function( arguments ) {

                apps[ 0 ].masterSecret = "0987654321";

                this.responseText = apps[ 0 ].simplePushVariants[ 0 ];
            }
        });

        App.reset();
        var controller = App.__container__.lookup("controller:variantIndex");
        this.controller = controller;
    },
    teardown: function() {
        $.mockjaxClear();
    }
});

test( "visit android variant page", function() {
    visit( "/mobileApps/variant/12345/android/12345" ).then( function() {
        equal( exists( "h2:contains('Android Version')" ), true, "Should be on Variant Page but isn't" );
    });
});

test( "test click 'Android Installation' link", function() {
    visit( "/mobileApps/variant/12345/android/12345" )
    .click( "table tbody tr td:eq(0) a" )
    .then( function() {
        equal( exists( "table tbody tr td:eq(0):contains('Android Installation')" ), true, "Should be on the Installation page, but not" );
    });
});

test( "visit ios variant page", function() {
    visit( "/mobileApps/variant/12345/ios/12345" ).then( function() {
        equal( exists( "h2:contains('iOS Version')" ), true, "Should be on Variant Page but isn't" );
    });
});

test( "test click 'iOS Installation' link", function() {
    visit( "/mobileApps/variant/12345/ios/12345" )
    .click( "table tbody tr td:eq(0) a" )
    .then( function() {
        equal( exists( "table tbody tr td:eq(0):contains('iOS Installation')" ), true, "Should be on the Installation page, but not" );
    });
});

test( "visit chrome packaged app variant page", function() {
    visit( "/mobileApps/variant/12345/chrome/12345" ).then( function() {
        equal( exists( "h2:contains('Chrome Packaged App Version')" ), true, "Should be on Variant Page but isn't" );
    });
});

test( "test click 'Chrome Packaged App Installation' link", function() {
    visit( "/mobileApps/variant/12345/chrome/12345" )
    .click( "table tbody tr td:eq(0) a" )
    .then( function() {
        equal( exists( "table tbody tr td:eq(0):contains('Chrome Packaged App Installation')" ), true, "Should be on the Installation page, but not" );
    });
});

test( "visit simple push variant page", function() {
    visit( "/mobileApps/variant/12345/simplePush/12345" ).then( function() {
        equal( exists( "h2:contains('Simple Push Version')" ), true, "Should be on Variant Page but isn't" );
    });
});

test( "test click 'Simple Push App Installation' link", function() {
    visit( "/mobileApps/variant/12345/simplePush/12345" )
    .click( "table tbody tr td:eq(0) a" )
    .then( function() {
        equal( exists( "table tbody tr td:eq(0):contains('Simple Push Installation')"), true, "Should be on the Installation page, but not" );
    });
});

test( "test refresh android secret link", function() {
    var that = this,
        oldSecret;
    visit( "/mobileApps/variant/12345/android/12345" )
    .then( function() {
        // save the master secret for later testing
        oldMasterSecret = $("section div:contains('Secret:') input")[1].value;
    })
    .click( "a:contains('Renew')" ) // Click the renew button
    .then( function() {
        // Reset Dialog should be there
        equal( $( "aside h1:contains('Renew Variant Secret')" ).parent().hasClass( 'hidden' ), false, "Reset Dialog should be visible but is not" );
    })
    .click( "aside button:contains('Cancel')" ) // Click No to remove the dialog
    .then( function() {
        equal( $( "aside h1:contains('Renew Variant Secret')" ).parent().hasClass( 'hidden' ), true, "Reset Dialog should not be visible but is" );
    })
    .click( "a:contains('Renew')" ) // Click the renew button again
    .click( "aside button:contains('Renew Variant Secret')" )
    .then( function() {
        // check that master secret is differnt
        var newSecret = $("section div:contains('Secret:') input")[1].value;
        notEqual( oldSecret, newSecret, "Secrets should be different but are not" );
        equal( $( "aside h1:contains('Renew Variant Secret')" ).parent().hasClass( 'hidden' ), true, "Reset Dialog should not be visible but is" );
    });
});

test( "test refresh ios secret link", function() {
    var that = this,
        oldSecret;
    visit( "/mobileApps/variant/12345/ios/12345" )
    .then( function() {
        // save the master secret for later testing
        oldMasterSecret = $("section div:contains('Secret:') input")[1].value;
    })
    .click( "a:contains('Renew')" ) // Click the renew button
    .then( function() {
        // Reset Dialog should be there
        equal( $( "aside h1:contains('Renew Variant Secret')" ).parent().hasClass( 'hidden' ), false, "Reset Dialog should be visible but is not" );
    })
    .click( "aside button:contains('Cancel')" ) // Click No to remove the dialog
    .then( function() {
        equal( $( "aside h1:contains('Renew Variant Secret')" ).parent().hasClass( 'hidden' ), true, "Reset Dialog should not be visible but is" );
    })
    .click( "a:contains('Renew')" ) // Click the renew button again
    .click( "aside button:contains('Renew Variant Secret')" )
    .then( function() {
        // check that master secret is differnt
        var newSecret = $("section div:contains('Secret:') input")[1].value;
        notEqual( oldSecret, newSecret, "Secrets should be different but are not" );
        equal( $( "aside h1:contains('Renew Variant Secret')" ).parent().hasClass( 'hidden' ), true, "Reset Dialog should not be visible but is" );
    });
});

test( "test refresh chrome packaged app secret link", function() {
    var that = this,
        oldSecret;
    visit( "/mobileApps/variant/12345/chrome/12345" )
    .then( function() {
        // save the master secret for later testing
        oldMasterSecret = $("section div:contains('Secret:') input")[1].value;
    })
    .click( "a:contains('Renew')" ) // Click the renew button
    .then( function() {
        // Reset Dialog should be there
        equal( $( "aside h1:contains('Renew Variant Secret')" ).parent().hasClass( 'hidden' ), false, "Reset Dialog should be visible but is not" );
    })
    .click( "aside button:contains('Cancel')" ) // Click No to remove the dialog
    .then( function() {
        equal( $( "aside h1:contains('Renew Variant Secret')" ).parent().hasClass( 'hidden' ), true, "Reset Dialog should not be visible but is" );
    })
    .click( "a:contains('Renew')" ) // Click the renew button again
    .click( "aside button:contains('Renew Variant Secret')" )
    .then( function() {
        // check that master secret is differnt
        var newSecret = $("section div:contains('Secret:') input")[1].value;
        notEqual( oldSecret, newSecret, "Secrets should be different but are not" );
        equal( $( "aside h1:contains('Renew Variant Secret')" ).parent().hasClass( 'hidden' ), true, "Reset Dialog should not be visible but is" );
    });
});


test( "test refresh simple push secret link", function() {
    var that = this,
        oldSecret;
    visit( "/mobileApps/variant/12345/simplePush/12345" )
    .then( function() {
        // save the master secret for later testing
        oldMasterSecret = $("section div:contains('Secret:') input")[1].value;
    })
    .click( "a:contains('Renew')" ) // Click the renew button
    .then( function() {
        // Reset Dialog should be there
        equal( $( "aside h1:contains('Renew Variant Secret')" ).parent().hasClass( 'hidden' ), false, "Reset Dialog should be visible but is not" );
    })
    .click( "aside button:contains('Cancel')" ) // Click No to remove the dialog
    .then( function() {
        equal( $( "aside h1:contains('Renew Variant Secret')" ).parent().hasClass( 'hidden' ), true, "Reset Dialog should not be visible but is" );
    })
    .click( "a:contains('Renew')" ) // Click the renew button again
    .click( "aside button:contains('Renew Variant Secret')" )
    .then( function() {
        // check that master secret is differnt
        var newSecret = $("section div:contains('Secret:') input")[1].value;
        notEqual( oldSecret, newSecret, "Secrets should be different but are not" );
        equal( $( "aside h1:contains('Renew Variant Secret')" ).parent().hasClass( 'hidden' ), true, "Reset Dialog should not be visible but is" );
    });
});

