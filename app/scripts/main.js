var adminApp = {
    init: function() {
        this.aerogear = {};

        //Create AeroGear Authentication module
        this.aerogear.authenticator = AeroGear.Auth({
            name: "authenticator",
            settings: {
                baseURL: "http://localhost:8080/ag-push/rest/"
            }
        }).modules.authenticator;

        //Create AeroGear pipe
        this.aerogear.pipelines = AeroGear.Pipeline([
            {
                name: "applications",
                settings: {
                    id: "pushApplicationID",
                    baseURL: "http://localhost:8080/ag-push/rest/",
                    authenticator: this.aerogear.authenticator
                }
            },
            {   //Might not be needed here,  just on device?
                name: "registration",
                settings: {
                    baseURL: "http://localhost:8080/ag-push/rest/",
                    authenticator: this.aerogear.authenticator,
                    endpoint: "registry/device"
                }
            }
        ]);

        //Create AeroGear DataManager store
        //this.aerogear.applicationsStore = AeroGear.DataManager( "applicationsStore" ).stores.applicationsStore;

        console.log( "initialized" );
    },
    forceLogin: function() {
        //Cleanup?
        //route to the login page
        console.log( "going to login page" );
    },
    login: function() {
        var data = JSON.stringify( { "loginName": "admin", "password": "123" } );
        this.aerogear.authenticator.login( data, {
            contentType: "application/json",
            success: function( success ) {
                console.log( "Logged in", success );
            },
            error: function( error ) {
                console.log( "Error Logging in", error );
            }
        });
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
};

adminApp.init();
