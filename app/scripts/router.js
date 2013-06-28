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
        this.resource( "variants", { path: "variants/:mobileApplication_id" }, function() {

            // The Route for creating a new Mobile Application Variant
            this.route( "new" );

            // The Route for the variants detail
            this.resource( "variant", { path: "variant/:mobileApplication_id/:type/:mobileVariant_id" }, function() {

            });
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
App.VariantsRoute = Ember.Route.extend({
    /*model: function( params ) {
        App.MobileApplication.find( params.mobileApplication_id );
    },*/
    setupController: function( controller, model ) {

        // Force a refresh of this model when coming in from a {{#linkTo}}
        controller.set( "model", model.pushApplicationID ? App.MobileApplication.find( model.pushApplicationID ) : model );

    },
    serialize: function( model ) {

        // Make our non uniform id of pushApplicationID what ember expects
        return { mobileApplication_id: model.pushApplicationID };

    }
});

App.VariantRoute = Ember.Route.extend({
    model: function( params ) {
        return App.MobileVariant.find( params.mobileApplication_id, params.type, params.mobileVariant_id );
    },
    setupController: function( controller, model ) {

        // Force a refresh of this model when coming in from a {{#linkTo}}
        var pushApplicationID = this.controllerFor( "variants" ).get( "model" ).get( "pushApplicationID" );
        controller.set( "model", model.variantID ? App.MobileVariant.find( pushApplicationID, model.get( "type" ), model.variantID ) : model );
    },
    serialize: function( model ) {

        // Make our non uniform id of pushApplicationID what ember expects
        return {  mobileVariant_id: model.variantID, mobileApplication_id: this.modelFor( "variants" ).get( "pushApplicationID" ) ,type: model.get( "type" ) };

    },
    renderTemplate: function() {
        this.render( "variant", {
            into: "mobileApps"
        });
    }
});
