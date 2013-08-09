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

//TODO: test create link

//TODO test edit link

//TODO: test remove

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

test( "visit mobile apps page", function() {
    visit( "/mobileApps/edit/undefined" ).then( function() {
        equal( find("header h1").text().trim(), "Create Push Application", "Should be on the Create page, but not" );
    });
});

test( "Create new Mobile App - Empty Values", function() {
    var that = this;

    visit( "/mobileApps/edit/undefined" ).then( function() {
        click( "input[type='submit']" );
    }).then( function() {
        equal( exists( ".errors" ), true, "error class should exists but doesn't" );
    });
});

test( "Create new Mobile App - With Value", function() {
    var that = this;

    visit( "/mobileApps/edit/undefined" ).then( function() {
        fillIn( ".name", "Cool App" );
        fillIn( ".description", "Cool App Description" );
        click( "input[type='submit']" );
    }).then( function() {
        equal( exists( ".errors" ), false, "error class should exists but doesn't" );
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
