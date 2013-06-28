/* JBoss, Home of Professional Open Source
* Copyright Red Hat, Inc., and individual contributors
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* http://www.apache.org/licenses/LICENSE-2.0
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

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
