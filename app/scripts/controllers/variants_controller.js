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
