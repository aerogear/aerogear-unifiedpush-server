module('App.MobileAppsIndexController', {
    setup: function() {
        //Setup Mocks that we need for this Module
        var apps = [
            {
                "id": "4028818b3fe37e75013fe38200200000",
                "name": "Cool App 1",
                "description": "A Cool App for testing",
                "pushApplicationID": "12345",
                "masterSecret": "3ababa8f-cc35-455b-8fc1-311ffe206538",
            },
            {
                "id": "4028818b3fe37e75013fe38200200000",
                "name": "Cool App 2",
                "description": "A Cool App for testing",
                "pushApplicationID": "12345",
                "masterSecret": "3ababa8f-cc35-455b-8fc1-311ffe206538",
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

        App.reset();
        var controller = App.__container__.lookup("controller:mobileAppsIndex");
        this.controller = controller;
    },
    teardown: function() {
        $.mockjaxClear();
    }
});

test( "visit mobile apps page", function() {
    visit( "/" ).then( function() {
        equal( exists( ".message" ), true, "welcome message should be here but isn't" );
    });
});

test( "total apps and table rows", function() {
    var that = this;
    visit( "/" ).then( function() {
        var rows = find("table tbody tr").length,
            totalApps = that.controller.get("totalApps");

        equal( totalApps, 2, "should be 2, instead is " + totalApps );
        equal( rows, 2, "should be 2 rows in the table, instead " + rows );
        equal( rows, totalApps, "table rows and computedProperty not N'Sync" );
    });
});

test( "test click 'Create' link", function() {
    visit( "/" )
    .click( ".table-create-btn" )
    .then( function() {
        equal( find( "header h1" ).text().trim(), "Create Application", "Should be on the Create page, but not" );
    });
});

test( "test click 'Edit' link", function() {
    visit( "/" )
    .click( ".action a:eq(0)" )
    .then( function() {
        equal( find( "header h1" ).text().trim(), "Rename Application", "Should be on the Edit page, but not" );
    });
});

test( "test click 'Push Application' link", function() {
    visit( "/" )
    .click( "table tbody tr td:eq(0) a" )
    .then( function() {
        equal( find("section div:eq(0)").text().indexOf( "Master Secret" ) > -1, true, "Should be on the Variant page, but not" );
    });
});

//Possibly Test the "Variant" commputed properties

//TODO: test remove once we get rid of the crappy confirm box

module('App.MobileAppsEditController - Create New', {
    setup: function() {
        //Setup Mocks that we need for this Module
        var apps = [
            {
                "id": "4028818b3fe37e75013fe38200200000",
                "name": "Cool App 1",
                "description": "A Cool App for testing",
                "pushApplicationID": "12345",
                "masterSecret": "3ababa8f-cc35-455b-8fc1-311ffe206538",
            },
            {
                "id": "4028818b3fe37e75013fe38200200000",
                "name": "Cool App 2",
                "description": "A Cool App for testing",
                "pushApplicationID": "12345",
                "masterSecret": "3ababa8f-cc35-455b-8fc1-311ffe206538",
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
            url: App.baseURL + "rest/applications",
            type: "POST",
            dataType: 'json',
            response: function( arguments ) {

                apps.push( apps[0] );
                this.responseText = apps;
            }
        });

        App.reset();
        var controller = App.__container__.lookup("controller:mobileAppsEdit");
        this.controller = controller;
    },
    teardown: function() {
        $.mockjaxClear();
    }
});

test( "visit push apps edit page - Create", function() {
    visit( "/mobileApps/edit/undefined" ).then( function() {
        equal( find("header h1").text().trim(), "Create Application", "Should be on the Create page, but not" );
    });
});

test( "Create new Push App - Empty Values", function() {
    var that = this;
    visit( "/mobileApps/edit/undefined" )
    .click( "input[type='submit']" )
    .then( function() {
        var model = that.controller.get( "model" );

        equal( exists( ".errors" ), true, "error class should exists but doesn't" );
        equal( model.get( "isValid" ), false );
    });
});

test( "Create new Push App - With Value", function() {
    var that = this;

    visit( "/mobileApps/edit/undefined" )
    .fillIn( ".name", "Cool App" )
    .fillIn( ".description", "Cool App Description" )
    .click( "input[type='submit']" )
    .then(function() {
        that.model = that.controller.get( "model" );
        equal( that.model.get( "name" ), "Cool App" );
        equal( that.model.get( "description" ), "Cool App Description" );
        equal( that.model.get( "isValid" ), true );
        equal( exists( ".errors" ), false, "error class should not exists but does" );
        })
    .then( function(){
        wait().then( function() {
            var controller = App.__container__.lookup("controller:mobileAppsIndex"),
                rows = find("table tbody tr").length,
                totalApps = controller.get("totalApps");

            equal( exists( ".message" ), true, "welcome message should be here but isn't" );

            equal( totalApps, 3, "should be 3, instead is " + totalApps );
            equal( rows, 3, "should be 3 rows in the table, instead " + rows );
            equal( rows, totalApps, "table rows and computedProperty not N'Sync" );
        });
    });
});

test( "Create new Push App - test cancel", function() {
    visit( "/mobileApps/edit/undefined" )
    .click( "input[type='reset']" )
    .then( function() {
        equal( exists( ".message" ), true, "welcome message should be here but isn't" );
    });
});

module('App.MobileAppsEditController - Edit', {
    setup: function() {
        //Setup Mocks that we need for this Module
        var apps = [
            {
                "id": "4028818b3fe37e75013fe38200200000",
                "name": "Cool App 1",
                "description": "A Cool App for testing",
                "pushApplicationID": "12345",
                "masterSecret": "3ababa8f-cc35-455b-8fc1-311ffe206538",
            },
            {
                "id": "4028818b3fe37e75013fe38200200000",
                "name": "Cool App 2",
                "description": "A Cool App for testing",
                "pushApplicationID": "12345",
                "masterSecret": "3ababa8f-cc35-455b-8fc1-311ffe206538",
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
            type: "PUT",
            dataType: 'json',
            response: function( arguments ) {
                var data = JSON.parse(arguments.data),
                    name = data.name,
                    description = data.description;

                apps[ 0 ].name = name;
                apps[ 0 ].description = description;

                this.responseText = apps[ 0 ];
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

        App.reset();
        var controller = App.__container__.lookup("controller:mobileAppsEdit");
        this.controller = controller;
    },
    teardown: function() {
        $.mockjaxClear();
    }
});

test( "visit push apps edit page - Edit", function() {
    var that = this;
    visit( "/mobileApps/edit/12345" ).then( function() {
        var model = that.controller.get( "model" ),
            name = find( ".name" ).val().trim(),
            description = find( ".description" ).val().trim();

        equal( find("header h1").text().trim(), "Rename Application", "Should be on the Create page, but not" );

        equal( model.get( "name" ), "Cool App 1" );
        equal( model.get( "description" ), "A Cool App for testing" );

        equal( name, "Cool App 1", "name is not what is excpected" );
        equal( description, "A Cool App for testing", "description is not what is excpected" );

        equal( model.get( "name" ), name );
        equal( model.get( "description" ), description );
    });
});

test( "Edit Push Application", function() {
    var that = this;
    var name = "New Name",
        description = "New Description";
    visit( "/mobileApps/edit/12345" )
    .fillIn( ".name", name )
    .fillIn( ".description", description )
    .click( "input[type='submit']" )
    .then(function() {
        that.model = that.controller.get( "model" );
        equal( that.model.get( "name" ), name );
        equal( that.model.get( "description" ), description );
        equal( exists( ".errors" ), false, "error class should not exists but does" );
    })
    .then( function() {
        wait().then( function() {
            //TODO: a Check that the updated record is there
        });
    });
});

test( "Edit Push Application - test cancel", function() {
    visit( "/mobileApps/edit/undefined" )
    .click( "input[type='reset']" )
    .then( function() {
        equal( exists( ".message" ), true, "welcome message should be here but isn't" );
    });
});
