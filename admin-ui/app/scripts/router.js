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

        // The Route for editing a Mobile Application Name and Description
        this.route( "edit", { path: "edit/:mobileApplication_id" } );

        // The Nested Route for seeing a list of variants for a mobile application
        this.resource( "variants", { path: "variants/:mobileApplication_id" }, function() {

            // The Route for creating a new Mobile Application Variant
            this.route( "add", { path: "add/:mobileApplication_id"} ); //Not really thrilled by this

            // The Route for composing a push message
            this.route( "compose", { path: "compose/:mobileApplication_id"} ); //Not really thrilled by this

            // The Route for editing a new Mobile Application Variant
            this.route( "edit", { path: "edit/:mobileApplication_id/:type/:mobileVariant_id" } ); //Not really thrilled by this either
        });

        // The Route for the variants detail, shows the list of instances
        this.resource( "variant", { path: "variant/:mobileApplication_id/:type/:mobileVariant_id" }, function() {
            this.route( "snippets", { path: "snippets/:mobileApplication_id/:type/:mobileVariant_id"} );
        });

        // The Route for showing the detail of an instance
        this.resource( "instance", { path: "instances/:mobileApplication_id/:type/:mobileVariant_id/:mobileVariantInstance_id" }, function() {} );
    });
});

/*
    Application Route - Mostly For Global Events
*/
App.ApplicationRoute = Ember.Route.extend({
    actions: {
        error: function( controller, errormsg ) {
            //enable again any submit button
            controller.get('controllers.application' ).set( "isProcessing", false );

            var content = controller.get( "content" ),
                templateTarget;
            if( content ) {
                controller.set( "content.errors", errormsg );
            } else {
                controller.set( "content", { "errors": errormsg } );
            }

            if( AeroGear.isArray( errormsg ) ) {
                templateTarget = "error";
            } else {
                templateTarget = "error_string";
            }
            this.render( templateTarget, { into: "application", outlet: "error", controller: controller } );
        },
        clearErrors: function(){
            this.render( "nothing", { into: "application", outlet: "error" } );
        }
    }
});

/*
    Login Route
*/
App.LoginRoute = Ember.Route.extend({
    activate: function(){
        this.send( "clearErrors" );
    },
    model: function() {

        return App.User.create();
    }
});

/*
    Application Index Route
*/
App.IndexRoute = App.Route.extend({
    redirect: function() {

        // Redirect to /mobileApps
        this.transitionTo( "mobileApps" );

    }
});

/*
    Mobile Applications Index Route

    Load All Mobile Applications
*/
App.MobileAppsIndexRoute = App.Route.extend({
    model: function() {

        // Return All the Mobile Applications
        return App.MobileApplication.find();

    }
});

/*
    Mobile Applications Edit Route

    Create/Edit A Mobile Application
*/
App.MobileAppsEditRoute = App.Route.extend({
    model: function( param ) {

        //don't like the formatting here
        return ( param.mobileApplication_id === "undefined" || param.mobileApplication_id === undefined ) ? App.MobileApplication.create() : App.MobileApplication.find( param.mobileApplication_id );
    },
    serialize: function( model ) {

        // Make our non uniform id's what ember expects
        return { mobileApplication_id: model ? model.pushApplicationID : "" };

    }
});

/*
    Route for Application Variants - shows a list of variants
    Don't need the model since ember  will do find( id ) by default
*/
App.VariantsRoute = App.Route.extend({
    serialize: function( model ) {

        // Make our non uniform id's what ember expects
        return { mobileApplication_id: model.pushApplicationID };

    }
});

/*
    Route for Application Variants Index Page
*/
App.VariantsIndexRoute = App.Route.extend({
    model: function() {

        // Return the "Variants" Route Model since that is where all the "dynamic segments" are
        return this.modelFor( "variants" );

    },
    setupController: function( controller, model ) {

        //Load the current Model
        controller.set( "model", model.pushApplicationID ? App.MobileApplication.find( model.pushApplicationID ) : model );

    },
    serialize: function() {

        // Make our non uniform id's what ember expects
        return { mobileApplication_id: this.modelFor( "variants" ).get( "pushApplicationID" ) };

    }
});

/*
    Route for adding a variant
*/
App.VariantsAddRoute = App.Route.extend({
    model: function( params ) {

        // Return the "Variants" Route Model since that is where all the "dynamic segments" are
        return App.MobileVariant.create({
            pushApplicationID: params.mobileApplication_id
        });

    },
    setupController: function( controller, model ) {
        var myModel;

        if( model instanceof App.MobileVariant ) {
            myModel = model;
        } else {
            myModel = App.MobileVariant.create({
                pushApplicationID: model.get( "pushApplicationID" )
            });
        }
        controller.set( "model", myModel );
    },
    serialize: function( model ) {

        // Make our non uniform id's what ember expects
        return { mobileApplication_id: model.get( "pushApplicationID" ) };

    }
});

App.VariantsComposeRoute = App.Route.extend({
    model: function() {

        // Return the "Variants" Route Model since that is where all the "dynamic segments" are
        return this.modelFor( "variants" );

    },
    setupController: function( controller, model ) {

        //Load the current Model
        controller.set( "model", model.pushApplicationID ? App.MobileApplication.find( model.pushApplicationID ) : model );

    },
    serialize: function() {

        // Make our non uniform id's what ember expects
        return { mobileApplication_id: this.modelFor( "variants" ).get( "pushApplicationID" ) };

    }
});


/*
    Route for adding/editing a variant
*/
App.VariantsEditRoute = App.Route.extend({
    model: function( params ) {

        // Return the "Variants" Route Model since that is where all the "dynamic segments" are
        return App.MobileVariant.find( params.mobileApplication_id, params.type, params.mobileVariant_id );

    },
    serialize: function( model ) {

        // Make our non uniform id's what ember expects
        return { mobileApplication_id: model.get( "pushApplicationID" ), mobileVariant_id: model.get( "variantID" ), type: model.get( "vType" ) };

    }
});


/*
    Route for the Single Variant - shows a list a instances
*/
App.VariantRoute = App.Route.extend({
    model: function( params ) {

        // Return All the mobile variants
        return App.MobileVariant.find( params.mobileApplication_id, params.type, params.mobileVariant_id );

    },
    serialize: function( model ) {

        // Make our non uniform id's what ember expects
        return {  mobileVariant_id: model.variantID, mobileApplication_id:  model.pushApplicationID ,type: model.variantType ? model.variantType : model.get( "vType" ) };

    }
});


/*
    Route for the Single Variant index page
*/
App.VariantIndexRoute = App.Route.extend({
    model: function() {

        // Return the "Variant" Route Model since that is where all the "dynamic segments" are
        return this.modelFor( "variant" );
    },
    setupController: function( controller, model ) {

        // Force a refresh of this model when coming in from a {{#linkTo}}
        controller.set( "model", model.variantID ? App.MobileVariant.find( model.pushApplicationID, model.variantType ? model.variantType : model.get( "vType" ), model.variantID ) : model );

    },
    serialize: function( model ) {

        // Make our non uniform id's what ember expects
        return {  mobileVariant_id: model.variantID, mobileApplication_id: this.modelFor( "variant" ).get( "pushApplicationID" ) ,type: model.get( "vType" ) };

    }
});

/*
 Route for the Single Variant index page
 */
App.VariantSnippetsRoute = App.Route.extend({
    model: function( params ) {

        return App.MobileVariant.find( params.mobileApplication_id, params.type, params.mobileVariant_id );
    },
    serialize: function( model ) {

        // Make our non uniform id's what ember expects
        return {  mobileVariant_id: model.variantID, mobileApplication_id:  model.pushApplicationID ,type: model.variantType ? model.variantType : model.get( "vType" ) };

    }
});


App.VariantSnippetsIndexRoute = App.Route.extend({
    model: function() {

        // Return the "Variant" Route Model since that is where all the "dynamic segments" are
        return this.modelFor( "variant" );
    },
    setupController: function( controller, model ) {

        // Force a refresh of this model when coming in from a {{#linkTo}}
        controller.set( "model", model.variantID ? App.MobileVariant.find( model.pushApplicationID, model.variantType ? model.variantType : model.get( "vType" ), model.variantID ) : model );

    },
    serialize: function( model ) {

        // Make our non uniform id's what ember expects
        return {  mobileVariant_id: model.variantID, mobileApplication_id: this.modelFor( "variant" ).get( "pushApplicationID" ) ,type: model.get( "vType" ) };

    }
});

/*
    Route for an Instance
*/
App.InstanceRoute = App.Route.extend({
    model: function( params ) {

        // Return All the instances of the variants from the params
        return App.MobileVariantInstance.find( params.mobileApplication_id, params.type, params.mobileVariant_id, params.mobileVariantInstance_id );

    },
    serialize: function( model ) {

        // Make our non uniform id's what ember expects
        return {  mobileVariantInstance_id: model.id, mobileVariant_id: model.variantID, mobileApplication_id:  model.pushApplicationID ,type: model.get( "vType" ) };

    }
});


/*
    Route for an Instance index page
*/
App.InstanceIndexRoute = App.Route.extend({
    model: function() {

        // Return the "instance" Route Model since that is where all the "dynamic segments" are
        return this.modelFor( "instance" );

    },
    setupController: function( controller, model ) {

        // Force a refresh of this model when coming in from a {{#linkTo}}
        controller.set( "model", model.id ? App.MobileVariantInstance.find( model.pushApplicationID, model.get( "vType" ), model.variantID, model.id ) : model );

    },
    serialize: function( model ) {

        // Make our non uniform id's what ember expects
        return {  mobileVariantInstance_id: model.id, mobileVariant_id: model.variantID, mobileApplication_id:  this.modelFor( "instance" ).get( "pushApplicationID" ) ,type: model.get( "vType" ) };

    }
});
