/*var adminApp = {
    saveMobileApplication: function( applicationData ) {
        var applicationPipe = this.aerogear.pipelines.pipes.applications;

        applicationPipe.save( applicationData, {
            success: function( response ) {
                console.log( "save successful", response );
            },
            error: function( error ) {
                console.log( "error saving", error );
                switch( error.status ) {
                case 401:
                    adminApp.forceLogin();
                    break;
                default:
                    break;
                }
            }
        });
    },
    removeMobileApplication: function() {
        console.log( "TODO" );
    },
    createMobileVariant: function( pushApplicationId, type ) {
        var mobileVariant = AeroGear.Pipeline({
            name: "mobileVariant",
            settings: {
                baseURL: "http://localhost:8080/ag-push/rest/applications/",
                authenticator: adminApp.aerogear.authenticator,
                endpoint:  pushApplicationId + "/" + type
            }
        }).pipes.mobileVariant,
        data = {
            googleKey: "1234567890",
            name: "Application1Android",
            description: "An Android Variant of the Mobile App"
        };

        mobileVariant.save( data, {
            success: function( response ) {
                console.log( "save successful", response );
            },
            error: function( error ) {
                console.log( "error saving", error );
                switch( error.status ) {
                case 401:
                    adminApp.forceLogin();
                    break;
                default:
                    break;
                }
            }
        });
    },
    getMobileVariant: function( pushApplicationId, variantId ) {
        console.log( pushApplicationId, variantId );
        console.log( "TODO" );
    },
    updateMobileVariant: function( data, type ) {
        console.log( data, type );

        console.log( "TODO" );
    },
    removeMobileVariant: function( pushApplicationId, variantId ) {
        console.log( pushApplicationId, variantId );
        console.log( "TODO" );
    }
},*/

var App = Ember.Application.create({
    LOG_TRANSITIONS: true
});

/*
    Application Router

    Use resource for nested routes
*/
App.Router.map( function() {
    // The Login Page
    this.route( "login" );
    // The Main List of Mobile Applications and the main starting point.  This is a nested route since the header/footer will be similar.
    this.resource( "mobileApps", function() {
        this.resource( "appcreate" );
        this.resource( "app", { path: "app/:pushApplicationID" }, function(){
            this.resource( "variants" );
        });
    });
    // The Route for a single Mobile Application.
    /*this.resource( "app", { path: "app/:pushApplicationId"}, function() {
        this.resource( "variants" );
        this.resource( "instances" );
    });*/
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
    Ember Application Controller.
*/
//App.ApplicationController = Ember.Controller.extend({});

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
                    that.transitionTo( "mobileApps" );
                    console.log( "Error Logging in", error );
                }
            });
        }
    }
});

/*
    Mobile Applications Index Route
*/
App.MobileAppsIndexRoute = Ember.Route.extend({
    model: function() {
        return App.mobileApplicationsInstance;
    },
    setupController: function( controller, model ) {
        model.setProperties(this.getMobileApplication( model ));
    },
    getMobileApplication: function( model, applicationPushId ) {
        var applicationPipe = App.AeroGear.pipelines.pipes.applications,
            that = this;

        applicationPipe.read({
            id: applicationPushId,
            success: function( response ) {
                console.log( "application reponse", response );
                model.setProperties( { apps: response, isLoaded: true } );
            },
            error: function( error ) {
                console.log( "error with application endpoint", error );
                switch( error.status ) {
                case 401:
                    that.transitionTo( "login" );
                    break;
                default:
                    //that.transitionTo( "login" );
                    model.setProperties( { isLoaded: true, error: error } );
                    break;
                }
            }
        });
    }
});

/*
    The Mobile Apps Controller. Put "Global Events" Here
*/
App.MobileAppsController = Ember.Controller.extend({
    logout: function() {
        var that = this;
        App.AeroGear.authenticator.logout({
            contentType: "application/json",
            success: function( success ) {
                console.log( "Logged Out", success );
                that.transitionTo( "login" );
            },
            error: function( error ) {
                console.log( "Error Logging Out", error );
                that.transitionTo( "login" );
            }
        });
    }
});

/*
    The Controller for adding new Mobile apps
*/
App.AppcreateController = Ember.Controller.extend({
    add: function() {
        this.saveMobileApplication();
    },
    saveMobileApplication: function( applicationData ) {
        var applicationPipe = App.AeroGear.pipelines.pipes.applications,
            that = this;

        applicationPipe.save( applicationData, {
            success: function( response ) {
                console.log( response );
                that.transitionToRoute( "mobileApps" );
            },
            error: function( error ) {
                console.log( "error saving", error );
                switch( error.status ) {
                case 401:
                    that.transitionTo( "login" );
                    break;
                default:
                    break;
                }
            }
        });
    },
    cancel: function() {
        this.transitionToRoute( "mobileApps" );
        console.log( "cancel" );
    }
});

/*App.AppRoute = Ember.Route.extend({
    model: function() {
        console.log( this );
    }
});*/

//App.AppVariantsRoute = Ember.Route.extend({});

//App.AppNetworksRoute = Ember.Route.extend({});

//App.AppInstancesRoute = Ember.Route.extend({});

/*
An Object Representing a list of Mobile Apps
*/
App.MobileApplications = Ember.Object.extend({ isLoaded: false });


/*
An instance of the object
*/
App.mobileApplicationsInstance = App.MobileApplications.create( { apps: [] } );

/*
AeroGear related things
*/
App.AeroGear = {};

App.AeroGear.authenticator = AeroGear.Auth({
    name: "authenticator",
    settings: {
        baseURL: "http://localhost:8080/ag-push/rest/"
    }
}).modules.authenticator;

App.AeroGear.pipelines = AeroGear.Pipeline([
    {
        name: "applications",
        settings: {
            id: "pushApplicationID",
            baseURL: "ag-push/rest/",
            authenticator: App.AeroGear.authenticator
        }
    },
    {   //Might not be needed here,  just on device?
        name: "registration",
        settings: {
            baseURL: "ag-push/rest/",
            authenticator: App.AeroGear.authenticator,
            endpoint: "registry/device"
        }
    }
]);

// Serializes a form to a JavaScript Object
$.fn.serializeObject = function() {
    var o = {};
    var a = this.serializeArray();
    $.each( a, function() {
        if ( o[ this.name ] ) {
            if ( !o[ this.name ].push ) {
                o[ this.name ] = [ o[ this.name ] ];
            }
            o[ this.name ].push( this.value || '' );
        } else {
            o[ this.name ] = this.value || '';
        }
    });
    return o;
};
