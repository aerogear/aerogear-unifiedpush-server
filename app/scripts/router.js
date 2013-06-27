/*
    Application Router

    Use resource for nested routes
*/
App.Router.map( function() {
    // The Login Page
    this.route( "login" );
    // The Main List of Mobile Applications and the main starting point.  This is a nested route since the header/footer will be similar.
    this.resource( "mobileApps", function() {
        this.route( "new" );
        this.resource( "variant", { path: ":mobileApplication_id/variants" }, function() {
        });
    });
});

/*
    Application Index Route
*/
App.IndexRoute = Ember.Route.extend({
    redirect: function() {
        this.transitionTo( "mobileApps" );
    }
});

/*
    Login Route
*/
App.LoginRoute = Ember.Route.extend({
    events: {
        login: function() {
            var that = this,
                data = $( "form#login" ).serializeObject();

            App.AeroGear.authenticator.login( JSON.stringify( data ), {
                contentType: "application/json",
                success: function( success ) {
                    that.transitionTo( "mobileApps" );
                    console.log( "Logged in", success );
                },
                error: function( error ) {
                    console.log( "Error Logging in", error );
                }
            });
        }
    }
});

/*
    Mobile Applications Index Route

    Load All Mobile Applications or Load Just One
*/
App.MobileAppsIndexRoute = Ember.Route.extend({
    model: function() {
        return App.MobileApplication.find();
    }
});

/*
    Route for a Single App Variant
    Don't need the model this since it will do find( param ) by default
*/
App.VariantRoute = Ember.Route.extend({
    setupController: function( controller, model ) {
        controller.set( "model", model );
    },
    serialize: function( model ) {
        return { mobileApplication_id: model.pushApplicationID };
    }
});
