var adminApp = {
    init: function() {

        //this.aerogear.applicationsStore = AeroGear.DataManager( "applicationsStore" ).stores.applicationsStore;

        //Setup event handlers
        console.log( "initialized" );
    },
    logout: function() {
        this.aerogear.authenticator.logout({
            contentType: "application/json",
            success: function( success ) {
                console.log( "Logged Out", success );
                adminApp.forceLogin();
            },
            error: function( error ) {
                console.log( "Error Logging Out", error );
            }
        });
    },
    getMobileApplication: function( applicationId ) {
        var applicationPipe = this.aerogear.pipelines.pipes.applications;

        applicationPipe.read({
            id: applicationId,
            success: function( response ) {
                console.log( "application reponse", response );
            },
            error: function( error ) {
                console.log( "error with application endpoint", error );
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
},
App = Ember.Application.create();

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
            baseURL: "http://localhost:8080/ag-push/rest/",
            authenticator: App.AeroGear.authenticator
        }
    },
    {   //Might not be needed here,  just on device?
        name: "registration",
        settings: {
            baseURL: "http://localhost:8080/ag-push/rest/",
            authenticator: App.AeroGear.authenticator,
            endpoint: "registry/device"
        }
    }
]);

App.Router.map( function() {
    this.route( "main" );
    this.route( "login" );
});

App.ApplicationRoute = Ember.Route.extend({
    //Other Setup
});

App.IndexRoute = Ember.Route.extend({
    redirect: function(){
        this.transitionTo( "main" );
    }
});

App.MobileApp = Ember.Object.extend({});

App.MainRoute = Ember.Route.extend({
    model: function() {
        console.log( "model" );
        return App.MobileApp.create({"description": "woo"});
    },
    forceLogin: function() {
        this.transitionTo( "login" );
    },
    getMobileApplication: function( applicationId ) {
        var applicationPipe = App.AeroGear.pipelines.pipes.applications,
            that = this;

        applicationPipe.read({
            id: applicationId,
            success: function( response ) {
                console.log( "application reponse", response );
                return response[ 0 ];
            },
            error: function( error ) {
                console.log( "error with application endpoint", error );
                switch( error.status ) {
                case 401:
                    that.forceLogin();
                    break;
                default:
                    that.forceLogin();
                    break;
                }
            }
        });
    }
});

App.LoginRoute = Ember.Route.extend({
    events: {
        login: function() {
            var that = this,
                data = $( "form#login" ).serializeObject();

            App.AeroGear.authenticator.login( JSON.stringify( data ), {
                contentType: "application/json",
                success: function( success ) {
                    that.transitionTo( "main" );
                    console.log( "Logged in", success );
                },
                error: function( error ) {
                    console.log( "Error Logging in", error );
                }
            });
        }
    }
});



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
