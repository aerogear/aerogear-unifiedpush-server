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
    loginIn: true,
    login: function() {
        var that = this,
            user = this.get( "model" );

        //Validate the form fields with Ember Validations
        user.validate();

        if( !user.get( "isValid" ) ) {
            this.send( "error", this, "A Username and Password are required" );
        } else {
            // Use AeroGear Authenticator to login
            App.AeroGear.authenticator.login( JSON.stringify( { loginName: this.get( "loginName" ), password: this.get( "password" ) } ), {
                contentType: "application/json",
                success: function( response, statusText, jqXHR ) {
                    if( jqXHR.status === 403 ) {
                        //change the password
                        that.set( "loginIn", false );
                    } else {
                        // Successful Login, now go to /mobileApps
                        that.transitionToRoute( "mobileApps" );
                    }
                },
                error: function() {
                    that.send( "error", that, "Login Error" );
                }
            });
        }
    },
    //Only Temporary until we can get the user create scripts
    other: function() {
        //need to send to the update endpoint
        var that = this,
            password = this.get("password"),
            loginName = this.get( "loginName" ),
            data;

        data = JSON.stringify( { loginName: loginName, password: password } );

        $.ajax({
            url: App.baseURL + "rest/auth/update",
            type: "PUT",
            data: data,
            contentType: "application/json",
            success: function() {
                that.set( "loginIn", true );
                that.transitionToRoute( "mobileApps" );
            },
            error: function() {
                that.send( "error", that, "Save Error" );
            }
        });
    }
});
