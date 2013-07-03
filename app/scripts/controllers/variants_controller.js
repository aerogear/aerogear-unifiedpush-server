App.VariantsIndexController = Ember.ObjectController.extend({
    remove: function( variant ) {
        var things = variant,
            that = this,
            mobileVariantPipe = AeroGear.Pipeline({
            name: "mobileVariant",
            settings: {
                baseURL: "/ag-push/rest/applications/",
                authenticator: App.AeroGear.authenticator,
                endpoint:  variant.pushApplicationID + "/" + variant.get( "type" )
            }
        }).pipes.mobileVariant;

        mobileVariantPipe.remove( variant.variantID, {
            success: function() {
                var content = that.get("variantList"),
                    find;

                find = content.find( function( value ) {
                    return value.variantID === things.variantID;
                });

                content.removeObject( find );
            },
            error: function( error ) { // TODO: Maybe Make this a class method?
                console.log( "error with application endpoint", error );
                switch( error.status ) {
                case 401:
                    App.Router.router.transitionToRoute("login");
                    break;
                default:
                    //that.transitionToRoute( "login" );
                    //result.setProperties( { isLoaded: true, error: error } );
                    break;
                }
            }
        });
    }
});

App.VariantsAddController = Ember.ObjectController.extend({
    add: function() {
        var applicationData = {
            name: this.get( "variantName" ),
            googleKey: this.get( "googleKey" ),
            //applicationPushId: this.get( "pushApplicationID" ),
            //variantType: "android",
            description: this.get( "variantDescription" )
        },
        variantType = "android",
        applicationPushId = this.get( "pushApplicationID" );

        this.saveMobileVariant( applicationData, variantType, applicationPushId  );
    },
    saveMobileVariant: function( applicationData, variantType, applicationPushId ) {
        var mobileVariantPipe = AeroGear.Pipeline({
            name: "mobileVariant",
            settings: {
                baseURL: "/ag-push/rest/applications/",
                authenticator: App.AeroGear.authenticator,
                endpoint:  applicationPushId + "/" + variantType
            }
        }).pipes.mobileVariant,
        that = this;

        mobileVariantPipe.save( applicationData, {
            success: function() {
                that.transitionToRoute( "variants", that.get( "model" ) );
            },
            error: function( error ) {
                console.log( "error saving", error );
                switch( error.status ) {
                case 401:
                    that.transitionToRoute( "login" );
                    break;
                default:
                    break;
                }
            }
        });
    }
});
