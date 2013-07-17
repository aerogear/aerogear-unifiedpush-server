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
