/*
    Application Router

    Use resource for nested routes
*/
App.Router.map( function() {

    // The Login Page
    this.route( "login" );

    // The Main List of Mobile Applications and the main starting point.
    // This is a nested route since the header/footer will be similar.
    this.resource( "mobileApps", function() {

        // The Route for creating a New Mobile Application
        this.route( "new" );

        // The Nested Route for seeing a variants detail
        this.resource( "variant", { path: ":mobileApplication_id/variants" }, function() {

            // The Route for creating a new Mobile Application Variant
            this.route( "new" );

        });
    });
});

/*
    Application Index Route
*/
App.IndexRoute = Ember.Route.extend({
    redirect: function() {

        // Redirect to /mobileApps
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

            // Use AeroGear Authenticator to login
            App.AeroGear.authenticator.login( JSON.stringify( data ), {
                contentType: "application/json",
                success: function() {

                    // Successful Login, now go to /mobileApps
                    that.transitionTo( "mobileApps" );

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

    Load All Mobile Applications
*/
App.MobileAppsIndexRoute = Ember.Route.extend({
    model: function() {

        // Return All the Mobile Applications
        return App.MobileApplication.find();

    }
});

/*
    Route for a Single App Variant
    Don't need the model since ember  will do find( id ) by default
*/
App.VariantRoute = Ember.Route.extend({
    setupController: function( controller, model ) {

        // Force a refresh of this model when coming in from a {{#linkTo}}
        controller.set( "model", model.pushApplicationID ? App.MobileApplication.find( model.pushApplicationID ) : model );

    },
    serialize: function( model ) {

        // Make our non uniform id of pushApplicationID what ember expects
        return { mobileApplication_id: model.pushApplicationID };

    }
});
