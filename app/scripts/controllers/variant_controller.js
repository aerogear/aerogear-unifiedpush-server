App.VariantIndexController = Ember.ObjectController.extend({
    showReset: false,
    toggleResetOverlay: function() {
        if(this.get("showReset")){
            this.set("showReset",false);
        }
        else{
            this.set("showReset",true);
        }
    },
    reset: function( variant ) {
            var things = variant,
                that = this,
                mobileVariantPipe = AeroGear.Pipeline({
                    name: "mobileVariant",
                    settings: {
                        baseURL: App.baseURL + "rest/applications/",
                        authenticator: App.AeroGear.authenticator,
                        endpoint:  variant.pushApplicationID + "/" + variant.get( "vType") + "/" + variant.variantID
                    }
                }).pipes.mobileVariant;
            mobileVariantPipe.save( {id:"reset"}, {
                success: function( data ) {
                    things.set("secret",data['secret']);

                },
                error: function( error ) { // TODO: Maybe Make this a class method?
                    switch( error.status ) {
                        case 401:
                            App.Router.router.transitionToRoute( "login" );
                            break;
                        default:
                            that.send( "error", that, "Error Resetting" );
                            break;
                    }
                }
            });
            this.toggleResetOverlay();
        }
});