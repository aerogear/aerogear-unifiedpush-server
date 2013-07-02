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
        this.route( "add" );

        // The Route for editing a Mobile Application Name and Description
        this.route( "edit", { path: "edit/:mobileApplication_id" } );

        // The Nested Route for seeing a variants detail
        this.resource( "variants", { path: "variants/:mobileApplication_id" }, function() {

            // The Route for creating a new Mobile Application Variant
            this.route( "add" );

        });

        // The Route for the variants detail
        this.resource( "variant", { path: "variant/:mobileApplication_id/:type/:mobileVariant_id" }, function() {

            this.route( "edit" );

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
    Mobile Applications Index Route

    Load All Mobile Applications
*/
App.MobileAppsIndexRoute = Ember.Route.extend({
    model: function() {

        // Return All the Mobile Applications
        return App.MobileApplication.find();

    }
});

App.MobileAppsEditRoute = Ember.Route.extend({
    setupController: function( controller, model ) {
        controller.set( "model", model );
    },
    serialize: function( model ) {

        // Make our non uniform id of pushApplicationID what ember expects
        return { mobileApplication_id: model.pushApplicationID };

    }
});

/*
    Route for a Single App Variant
    Don't need the model since ember  will do find( id ) by default
*/
App.VariantsRoute = Ember.Route.extend({
    serialize: function( model ) {

        // Make our non uniform id of pushApplicationID what ember expects
        return { mobileApplication_id: model.pushApplicationID };

    }
});


App.VariantsIndexRoute = Ember.Route.extend({
    model: function() {
        return this.modelFor( "variants" );
    },
    setupController: function( controller, model ) {
        controller.set( "model", model.pushApplicationID ? App.MobileApplication.find( model.pushApplicationID ) : model );
    },
    serialize: function( model ) {

        // Make our non uniform id of pushApplicationID what ember expects
        return { mobileApplication_id: this.modelFor( "variants" ).get( "pushApplicationID" ) };

    }
});

App.VariantsAddRoute = Ember.Route.extend({
    model: function() {
        return this.modelFor( "variants" );
    },
    serialize: function( model ) {

        // Make our non uniform id of pushApplicationID what ember expects
        return { mobileApplication_id: this.modelFor( "variants" ).get( "pushApplicationID" ) };

    }
});

App.VariantRoute = Ember.Route.extend({
    model: function( params ) {

        // Return All the mobile variants
        return App.MobileVariant.find( params.mobileApplication_id, params.type, params.mobileVariant_id );

    },
    setupController: function( controller, model ) {

        // Get the pushApplicationID from the "variants" controller
        var pushApplicationID = this.controllerFor( "variants" ).get( "model" ).get( "pushApplicationID" );

        // Force a refresh of this model when coming in from a {{#linkTo}}
        controller.set( "model", model.variantID ? App.MobileVariant.find( pushApplicationID, model.get( "type" ), model.variantID ) : model );

    },
    serialize: function( model ) {

        // Make our non uniform id of pushApplicationID what ember expects
        return {  mobileVariant_id: model.variantID, mobileApplication_id: this.modelFor( "variants" ).get( "pushApplicationID" ) ,type: model.get( "type" ) };

    },
    renderTemplate: function() {

        // Tell Ember where to render our template view
        this.render( "variant", {
            into: "mobileApps"
        });
    }
});
