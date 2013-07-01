App.LoginController = Ember.ObjectController.extend({
    error: "",
    loginName: "",
    password: "",
    login: function() {
        var that = this;

        //TODO: more advanced validation

        if( !this.get( "password" ).trim().length ||  !this.get( "loginName" ).trim().length ) {
            this.set( "error", "A Username and Password are required" );
        } else {
            // Use AeroGear Authenticator to login
            App.AeroGear.authenticator.login( JSON.stringify( { loginName: this.loginName, password: this.password } ), {
                contentType: "application/json",
                success: function() {
                    // Successful Login, now go to /mobileApps
                    that.set( "error", "" );
                    that.transitionToRoute( "mobileApps" );
                },
                error: function( error ) {
                    //TODO: Show errors on screen
                    console.log( "Error Logging in", error );
                    that.set( "error", "Login Error" );
                }
            });
        }
    }
});
