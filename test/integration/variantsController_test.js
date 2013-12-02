module('App.VariantsIndexController', {
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
                        "description": "An Android Variant",
                        "type": "ANDROID",
                        "installations": [],
                    }
                ],
                "simplePushVariants": [],
                "iosvariants": []
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
            url: App.baseURL + "rest/applications/12345",
            type: "GET",
            dataType: 'json',
            response: function( arguments ) {
                this.responseText = apps[ 0 ];
            }
        });

        $.mockjax({
            url: App.baseURL + "rest/applications/12345/reset",
            type: "PUT",
            dataType: 'json',
            response: function( arguments ) {

                apps[ 0 ].masterSecret = "0987654321";

                this.responseText = apps[ 0 ];
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

        App.reset();
        var controller = App.__container__.lookup("controller:variantsIndex");
        this.controller = controller;
    },
    teardown: function() {
        $.mockjaxClear();
    }
});

test( "visit variants page", function() {
    visit( "/mobileApps/variants/12345" ).then( function() {
        equal( exists( "h3:contains('Variants')" ), true, "Should be on Variant Page but isn't" );
    });
});

test( "total variants and table rows", function() {
    var that = this;
    visit( "/mobileApps/variants/12345" ).then( function() {
        var rows = find("table tbody tr").length,
        totalVariants = that.controller.get("model").get("variantList").length;

        equal( totalVariants, 1, "should be 1, instead is " + totalVariants );
        equal( rows, 1, "should be 1 rows in the table, instead " + rows );
        equal( rows, totalVariants, "table rows and computedProperty not N'Sync" );
    });
});

test( "test refresh secret link", function() {
    var that = this,
        oldMasterSecret;
    visit( "/mobileApps/variants/12345" )
    .then( function() {
        // save the master secret for later testing
        oldMasterSecret = $("section div:contains('Master Secret:') input")[1].value;
    })
    .click( "a:contains('Renew')" ) // Click the renew button
    .then( function() {
        // Reset Dialog should be there
        equal( $( "aside h1:contains('Renew Master Secret')" ).parent().hasClass( 'hidden' ), false, "Reset Dialog should be visible but is not" );
    })
    .click( "aside button:contains('Cancel')" ) // Click No to remove the dialog
    .then( function() {
        equal( $( "aside h1:contains('Renew Master Secret')" ).parent().hasClass( 'hidden' ), true, "Reset Dialog should not be visible but is" );
    })
    .click( "a:contains('Renew')" ) // Click the renew button again
    .click( "aside button:contains('Renew Master Secret')" )
    .then( function() {
        // check that master secret is differnt
        var newMasterSecret = $("section div:contains('Master Secret:') input")[1].value;
        notEqual( oldMasterSecret, newMasterSecret, "Master Secrets should be different but are not" );
        equal( $( "aside h1:contains('Renew Master Secret')" ).parent().hasClass( 'hidden' ), true, "Reset Dialog should not be visible but is" );
    });
});

test( "test click 'Add' link", function() {
    visit( "/mobileApps/variants/12345" )
    .click( ".table-create-btn" )
    .then( function() {
        equal( find( "header h1" ).text().trim(), "Add Variant", "Should be on the Create page, but not" );
    });
});

test( "Add Variant - test cancel", function() {
    visit( "/mobileApps/variants/12345/add/12345" )
    .click( "input[type='reset']" )
    .then( function() {
        equal( exists( "h3:contains('Variants')" ), true, "Should be on Variant Page but isn't" );
    });
});


test( "test click 'Edit' link", function() {
    visit( "/mobileApps/variants/12345" )
    .click( ".action a:eq(0)" )
    .then( function() {
        equal( find( "header h1" ).text().trim(), "Edit a Variant", "Should be on the Create page, but not" );
    });
});

test( "Edit Variant - test cancel", function() {
    visit( "/mobileApps/variants/12345/edit/12345/android/12345" )
    .click( "input[type='reset']" )
    .then( function() {
        equal( exists( "h3:contains('Variants')" ), true, "Should be on Variant Page but isn't" );
    });
});


module('App.VariantsAddController - Add New', {
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
                        "description": "An Android Variant",
                        "type": "ANDROID",
                        "installations": [],
                    }
                ],
                "simplePushVariants": [],
                "iosvariants": [],
                "chromePackagedAppVariants": []
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
            url: App.baseURL + "rest/applications/12345",
            type: "GET",
            dataType: 'json',
            response: function( arguments ) {
                this.responseText = apps[ 0 ];
            }
        });

        $.mockjax({
            url: App.baseURL + "rest/applications/12345/android",
            type: "POST",
            dataType: 'json',
            response: function( arguments ) {
                var data = JSON.parse(arguments.data),
                    androidVariant = {
                        "id": "420",
                        "name": data.name,
                        "description": data.description,
                        "variantID": "12345",
                        "secret": "12345",
                        "type": "ANDROID",
                        "installations": [],
                        "googleKey": data.googleKey
                    };
                apps[ 0 ].androidVariants.push( androidVariant );
                this.responseText = androidVariant;
            }
        });

        $.mockjax({
            url: App.baseURL + "rest/applications/12345/simplePush",
            type: "POST",
            dataType: 'json',
            response: function( arguments ) {
                var data = JSON.parse(arguments.data),
                    simplePushVariant = {
                        "id": "421",
                        "name": data.name,
                        "description": data.description,
                        "variantID": "12345",
                        "secret": "12345",
                        "type": "SIMPLE_PUSH",
                        "installations": [],
                    };
                apps[ 0 ].simplePushVariants.push( simplePushVariant );
                this.responseText = simplePushVariant;
            }
        });

        $.mockjax({
            url: App.baseURL + "rest/applications/12345/chrome",
            type: "POST",
            dataType: 'json',
            response: function( arguments ) {
                var data = JSON.parse(arguments.data),
                    chromeVariant = {
                        "id": "422",
                        "name": data.name,
                        "description": data.description,
                        "variantID": "12345",
                        "secret": "12345",
                        "type": "CHROME_PACKAGED_APP",
                        "installations": [],
                        "clientId": data.clientId,
                        "clientSecret": data.clientSecret,
                        "refreshToken": data.refreshToken
                    };
                apps[ 0 ].chromePackagedAppVariants.push( chromeVariant );
                this.responseText = chromeVariant;
            }
        });

        // $.mockjax({
        //     url: App.baseURL + "rest/applications/12345/iOS",
        //     type: "POST",
        //     headers: {
        //         "Content-Type": "multipart/form-data"
        //     },
        //     response: function( arguments ) {
        //         //var data = JSON.parse(arguments.data),
        //         var iOSVariant = {
        //                 "id": "421",
        //                 "name": "Cool App",
        //                 "description": "Cool App Description",
        //                 "variantID": "12345",
        //                 "secret": "12345",
        //                 "type": "IOS",
        //                 "installations": [],
        //             };
        //         apps[ 0 ].iosvariants.push( iOSVariant );
        //         this.responseText = iOSVariant;
        //     }
        // });
        App.reset();
        var controller = App.__container__.lookup("controller:variantsAdd"),
            indexController = App.__container__.lookup("controller:variantsIndex");
        this.controller = controller;
        this.indexController = indexController;
    },
    teardown: function() {
        $.mockjaxClear();
    }
});

test( "Add Variant - submit with no values", function() {
    var that = this;
    visit( "/mobileApps/variants/12345/add/12345" )
    .click( "input[type='submit']" )
    .then( function() {
        var model = that.controller.get( "model" );

        equal( exists( ".errors" ), true, "error class should exists but doesn't" );
        equal( model.get( "isValid" ), false );
    });
});

test( "Add Variant - Google", function() {
    var that = this;

    visit( "/mobileApps/variants/12345/add/12345" )
    .fillIn( "input[name='name']" , "Cool App" )
    .fillIn( "textarea[name='description']", "Cool App Description" )
    .fillIn( "input[name='googleKey']", "abc123" )
    .click( "input[type='submit']" )
    .wait()
    .then(function( data ) {
        var model = that.indexController.get( "model" ).get( "variantList" ).findBy( "id", "420" );
        equal( model.get( "name" ), "Cool App" );
        equal( model.get( "description" ), "Cool App Description" );
        equal( model.get( "googleKey" ), "abc123" );
        equal( model.get( "type" ), "ANDROID" );
        equal( model.get( "vType" ), "android" );
        equal( model.get( "isValid" ), true );
        equal( exists( ".errors" ), false, "error class should not exists but does" );
    });
});

// TODO: this could be tricky becuase of certificate
// test( "Add Variant - iOS", function() {
//     var that = this;

//     visit( "/mobileApps/variants/12345/add/12345" )
//     .fillIn( "input[name='name']" , "Cool App" )
//     .fillIn( "textarea[name='description']", "Cool App Description" )
//     .click( "input[name='platform'][value='iOS']" )
//     .fillIn( "input[name='passphrase']", "super_secret" )
//     .then( function() {
//         var data = new Blob(['hello world'], {type: 'text/plain'});
//         that.controller.get( "model" ).set( "certificate", data );
//     })
//     .click( "input[type='submit']" )
//     .wait()
//     .then(function( data ) {
//         var model = that.indexController.get( "model" ).get( "variantList" ).findBy( "id", "421" );
//         equal( model.get( "name" ), "Cool App" );
//         equal( model.get( "description" ), "Cool App Description" );
//         equal( model.get( "type" ), "IOS" );
//         equal( model.get( "isValid" ), true );
//         equal( exists( ".errors" ), false, "error class should not exists but does" );
//     });
// });

test( "Add Variant - SimplePush", function() {
    var that = this;

    visit( "/mobileApps/variants/12345/add/12345" )
    .fillIn( "input[name='name']" , "Cool App" )
    .fillIn( "textarea[name='description']", "Cool App Description" )
    .click( "input[name='platform'][value='simplePush']" )
    .click( "input[type='submit']" )
    .wait()
    .then(function( data ) {
        var model = that.indexController.get( "model" ).get( "variantList" ).findBy( "id", "421" );
        equal( model.get( "name" ), "Cool App" );
        equal( model.get( "description" ), "Cool App Description" );
        equal( model.get( "type" ), "SIMPLE_PUSH" );
        equal( model.get( "vType" ), "simplePush" );
        equal( model.get( "isValid" ), true );
        equal( exists( ".errors" ), false, "error class should not exists but does" );
    });
});

test( "Add Variant - Chrome Packaged App", function() {
    var that = this;

    visit( "/mobileApps/variants/12345/add/12345" )
    .fillIn( "input[name='name']" , "Cool App" )
    .fillIn( "textarea[name='description']", "Cool App Description" )
    .fillIn( "input[name='clientId']", "abc123" )
    .fillIn( "input[name='clientSecret']", "shhhh" )
    .fillIn( "input[name='refreshToken']", "123_abc_456_def" )
    .click( "input[name='platform'][value='chrome']" )
    .click( "input[type='submit']" )
    .wait()
    .then(function( data ) {
        var model = that.indexController.get( "model" ).get( "variantList" ).findBy( "id", "422" );
        equal( model.get( "name" ), "Cool App" );
        equal( model.get( "description" ), "Cool App Description" );
        equal( model.get( "clientId" ), "abc123" );
        equal( model.get( "clientSecret" ), "shhhh" );
        equal( model.get( "refreshToken" ), "123_abc_456_def" );
        equal( model.get( "type" ), "CHROME_PACKAGED_APP" );
        equal( model.get( "vType" ), "chrome" );
        equal( model.get( "isValid" ), true );
        equal( exists( ".errors" ), false, "error class should not exists but does" );
    });
});

module('App.VariantsAddController - Edit', {
    setup: function() {
        var apps = [
            {
                "name": "Cool App 1",
                "description": "A Cool App for testing",
                "pushApplicationID": "12345",
                "masterSecret": "3ababa8f-cc35-455b-8fc1-311ffe206538",
                "androidVariants": [
                    {
                        "id": "420",
                        "name": "Cool Android App",
                        "description": "Cool Android App Description",
                        "variantID": "12345",
                        "secret": "12345",
                        "type": "ANDROID",
                        "installations": [],
                        "googleKey": "123abc"
                    }
                ],
                "simplePushVariants": [
                    {
                        "id": "421",
                        "name": "Cool SimplePush App",
                        "description": "Cool SimplePush App Description",
                        "variantID": "12345",
                        "secret": "12345",
                        "type": "SIMPLE_PUSH",
                        "installations": [],
                    }
                ],
                "iosvariants": [
                ],
                "chromePackagedAppVariants": [
                    {
                        "id": "422",
                        "name": "Cool Chrome Packaged App",
                        "description": "Cool Chrome Packaged App Description",
                        "variantID": "12345",
                        "secret": "12345",
                        "type": "CHROME_PACKAGED_APP",
                        "installations": [],
                        "clientId": "abc123",
                        "clientSecret": "shhhh",
                        "refreshToken": "abc_123_def"
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
            url: App.baseURL + "rest/applications/12345",
            type: "GET",
            dataType: 'json',
            response: function( arguments ) {
                this.responseText = apps[ 0 ];
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
            url: App.baseURL + "rest/applications/12345/android/12345",
            type: "PUT",
            dataType: 'json',
            response: function( arguments ) {
                var data = JSON.parse(arguments.data);
                this.responseText = $.extend( apps[0].androidVariants[0],data );
            }
        });

        $.mockjax({
            url: App.baseURL + "rest/applications/12345/simplePush/12345",
            type: "GET",
            dataType: 'json',
            response: function( arguments ) {
                this.responseText = apps[0].simplePushVariants[0];
            }
        });

        $.mockjax({
            url: App.baseURL + "rest/applications/12345/simplePush/12345",
            type: "PUT",
            dataType: 'json',
            response: function( arguments ) {
                var data = JSON.parse(arguments.data);
                this.responseText = $.extend( apps[0].simplePushVariants[0], data );
            }
        });

        $.mockjax({
            url: App.baseURL + "rest/applications/12345/chrome/12345",
            type: "GET",
            dataType: 'json',
            response: function( arguments ) {
                this.responseText = apps[0].chromePackagedAppVariants[0];
            }
        });

        $.mockjax({
            url: App.baseURL + "rest/applications/12345/chrome/12345",
            type: "PUT",
            dataType: 'json',
            response: function( arguments ) {
                var data = JSON.parse(arguments.data);
                this.responseText = $.extend( apps[0].chromePackagedAppVariants[0], data );
            }
        });

        // $.mockjax({
        //     url: App.baseURL + "rest/applications/12345/iOS",
        //     type: "POST",
        //     headers: {
        //         "Content-Type": "multipart/form-data"
        //     },
        //     response: function( arguments ) {
        //         //var data = JSON.parse(arguments.data),
        //         var iOSVariant = {
        //                 "id": "421",
        //                 "name": "Cool App",
        //                 "description": "Cool App Description",
        //                 "variantID": "12345",
        //                 "secret": "12345",
        //                 "type": "IOS",
        //                 "installations": [],
        //             };
        //         apps[ 0 ].iosvariants.push( iOSVariant );
        //         this.responseText = iOSVariant;
        //     }
        // });
        App.reset();
        var controller = App.__container__.lookup("controller:variantsEdit"),
            indexController = App.__container__.lookup("controller:variantsIndex");
        this.controller = controller;
        this.indexController = indexController;
    },
    teardown: function() {
        $.mockjaxClear();
    }
});

test( "Edit Variant - Google", function() {
    var that = this;
    visit( "/mobileApps/variants/12345/edit/12345/android/12345" )
    .then( function() {
        // Check that the fields are still filled out
        var model = that.controller.get( "model" );
        equal( model.get( "name" ), find("input[name='name']").val() );
        equal( model.get( "description" ), find("textarea[name='description']").val() );
        equal( model.get( "googleKey" ), find("input[name='googleKey']").val() );
    })
    .fillIn( "input[name='name']" , "Cool App Updated" )
    .fillIn( "textarea[name='description']", "Cool App Description Updated" )
    .fillIn( "input[name='googleKey']", "abc123def" )
    .click( "input[type='submit']" )
    .then( function() {
        var model = that.indexController.get( "model" ).get( "variantList" ).findBy( "id", "420" );
        equal( model.get( "name" ), "Cool App Updated" );
        equal( model.get( "description" ), "Cool App Description Updated" );
        equal( model.get( "googleKey" ), "abc123def" );
        equal( exists( ".errors" ), false, "error class should exists but doesn't" );
        equal( model.get( "isValid" ), true );
    });
});

// TODO: iOS edit - this could be tricky becuase of certificate

test( "Edit Variant - SimplePush", function() {
    var that = this;
    visit( "/mobileApps/variants/12345/edit/12345/simplePush/12345" )
    .then( function() {
        // Check that the fields are still filled out
        var model = that.controller.get( "model" );
        equal( model.get( "name" ), find("input[name='name']").val() );
        equal( model.get( "description" ), find("textarea[name='description']").val() );
    })
    .fillIn( "input[name='name']" , "Cool SimplePush App Updated" )
    .fillIn( "textarea[name='description']", "Cool SimplePush App Description Updated" )
    .click( "input[type='submit']" )
    .then( function() {
        var model = that.indexController.get( "model" ).get( "variantList" ).findBy( "id", "421" );
        equal( model.get( "name" ), "Cool SimplePush App Updated" );
        equal( model.get( "description" ), "Cool SimplePush App Description Updated" );
        equal( exists( ".errors" ), false, "error class should exists but doesn't" );
        equal( model.get( "isValid" ), true );
    });
});

test( "Edit Variant - Chrome Packaged App", function() {
    var that = this;

    visit( "/mobileApps/variants/12345/edit/12345/chrome/12345" ).
    then( function() {
         // Check that the fields are still filled out
        var model = that.controller.get( "model" );
        equal( model.get( "name" ), find("input[name='name']").val() );
        equal( model.get( "description" ), find("textarea[name='description']").val() );
        equal( model.get( "clientId" ), find("input[name='clientId']").val() );
        equal( model.get( "clientSecret" ), find("input[name='clientSecret']").val() );
        equal( model.get( "refreshToken" ), find("input[name='refreshToken']").val() );
    })
    .fillIn( "input[name='name']" , "Cool Chrome Packaged App Updated" )
    .fillIn( "textarea[name='description']", "Cool Chrome Packaged App Description Updated" )
    .fillIn( "input[name='clientId']", "abc123def" )
    .fillIn( "input[name='clientSecret']", "shhhh_shhhhh" )
    .fillIn( "input[name='refreshToken']", "123_abc_456_def_789" )
    .click( "input[type='submit']" )
    .wait()
    .then(function( data ) {
        var model = that.indexController.get( "model" ).get( "variantList" ).findBy( "id", "422" );
        equal( model.get( "name" ), "Cool Chrome Packaged App Updated" );
        equal( model.get( "description" ), "Cool Chrome Packaged App Description Updated" );
        equal( model.get( "clientId" ), "abc123def" );
        equal( model.get( "clientSecret" ), "shhhh_shhhhh" );
        equal( model.get( "refreshToken" ), "123_abc_456_def_789" );
        equal( model.get( "isValid" ), true );
        equal( exists( ".errors" ), false, "error class should not exists but does" );
    });
});
