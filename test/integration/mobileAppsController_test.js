module('App.MobileAppsIndexController', {
    setup: function() {
        App.reset();
        var controller = App.__container__.lookup("controller:mobileAppsIndex");
        this.controller = controller;
    },
    teardown: function() {
        //$.mockjaxClear();
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
    visit( "/" ).then( function() {
        click( ".table-create-btn" );
    }).then( function() {
        equal( find( "header h1" ).text().trim(), "Create Push Application", "Should be on the Create page, but not" );
    });
});

test( "test click 'Edit' link", function() {
    visit( "/" ).then( function() {
        click( ".action a:eq(0)" );
    }).then( function() {
        equal( find( "header h1" ).text().trim(), "Edit Push Application", "Should be on the Edit page, but not" );
    });
});

//TODO: test remove once we get rid of the crappy confirm box

module('App.MobileAppsEditController - Create New', {
    setup: function() {
        App.reset();
        var controller = App.__container__.lookup("controller:mobileAppsEdit");
        this.controller = controller;
    },
    teardown: function() {
        //$.mockjaxClear();
    }
});

test( "visit push apps edit page - Create", function() {
    visit( "/mobileApps/edit/undefined" ).then( function() {
        equal( find("header h1").text().trim(), "Create Push Application", "Should be on the Create page, but not" );
    });
});

test( "Create new Push App - Empty Values", function() {
    var that = this;
    visit( "/mobileApps/edit/undefined" ).then( function() {
        click( "input[type='submit']" );
    }).then( function() {
        var model = that.controller.get( "model" );

        equal( exists( ".errors" ), true, "error class should exists but doesn't" );
        equal( model.get( "isValid" ), false );
    });
});

test( "Create new Push App - With Value", function() {
    var that = this;

    visit( "/mobileApps/edit/undefined" ).then( function() {
        fillIn( ".name", "Cool App" );
        fillIn( ".description", "Cool App Description" );

        that.model = that.controller.get( "model" );

        equal( that.model.get( "name" ), "Cool App" );
        equal( that.model.get( "description" ), "Cool App Description" );

        click( "input[type='submit']" );
    }).then( function() {
        equal( that.model.get( "isValid" ), true );
        equal( exists( ".errors" ), false, "error class should not exists but does" );
    }).then( function(){
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

module('App.MobileAppsEditController - Edit', {
    setup: function() {
        App.reset();
        var controller = App.__container__.lookup("controller:mobileAppsEdit");
        this.controller = controller;
    },
    teardown: function() {
        //$.mockjaxClear();
    }
});

test( "visit push apps edit page - Edit", function() {
    var that = this;
    visit( "/mobileApps/edit/12345" ).then( function() {
        var model = that.controller.get( "model" ),
            name = find( ".name" ).val().trim(),
            description = find( ".description" ).val().trim();

        equal( find("header h1").text().trim(), "Edit Push Application", "Should be on the Create page, but not" );

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
    visit( "/mobileApps/edit/12345" ).then( function() {
        var name = "New Name",
            description = "New Description";

        that.model = that.controller.get( "model" );

        fillIn( ".name", name );
        fillIn( ".description", description );

        equal( that.model.get( "name" ), name );
        equal( that.model.get( "description" ), description );

        click( "input[type='submit']" );
    }).then( function() {
        equal( exists( ".errors" ), false, "error class should not exists but does" );
    }).then( function() {
        wait().then( function() {
            //TODO: a Check that the updated record is there
        });
    });
});
